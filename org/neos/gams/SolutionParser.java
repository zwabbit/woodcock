package org.neos.gams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neos.client.FileUtils;

public class SolutionParser {

	public static final int OPTIMAL = 1;
	public static final int INFEASIBLE = 4;

	static final Pattern header = Pattern.compile("LOWER\\s+LEVEL",
			Pattern.DOTALL);

	static final Pattern modelPattern = Pattern
			.compile("MODEL STATUS\\s+(\\d+)(.*)");
	static final Pattern solverPattern = Pattern
			.compile("SOLVER STATUS\\s+(\\d+)(.*)");

	static final Pattern objPattern = Pattern
			.compile("OBJECTIVE VALUE\\s+(.*)");

	String text;

	Double objective;
	String solverStatus;
	Integer solverStatusCode;
	String modelStatus;
	Integer modelStatusCode;

	/**
	 * Create a parser instance from GAMS output
	 * 
	 * @param text
	 *            output from GAMS
	 */
	public SolutionParser(String text) {

		if (text == null || text.equals("")) {
			System.err.println("Error parsing result");
			modelStatusCode = 99;
			modelStatus = "PARSING ERROR";
			solverStatusCode = 99;
			solverStatus = "PARSING ERROR";
			objective = Double.NaN;
			return;
		}

		this.text = text;

		Matcher m = modelPattern.matcher(text);
		if (m.find()) {
			modelStatusCode = new Integer(m.group(1));
			modelStatus = m.group(2).trim();
		} else {
			System.err.println("Error parsing model status");
			modelStatusCode = 99;
			modelStatus = "PARSING ERROR";
		}
		m = solverPattern.matcher(text);
		if (m.find()) {
			solverStatusCode = new Integer(m.group(1));
			solverStatus = m.group(2).trim();
		} else {
			System.err.println("Error parsing solver status");
			solverStatusCode = 99;
			solverStatus = "PARSING ERROR";
		}
		m = objPattern.matcher(text);
		if (m.find()) {
			objective = GAMSUtil.parseDouble(m.group(1));
		} else {
			System.err.println("Error parsing objective status");
			objective = Double.NaN;
		}
	}

	/**
	 * Extract output table with the specified name, type and dimension This
	 * method should be use only if the symbol actual exists in the output.
	 * 
	 * It may not report error if incorrect dimension is supplied, but the index
	 * or value may contain incorrect information.
	 * 
	 * @param name
	 *            Name of the symbol
	 * @param type
	 *            Type of the symbol (EQU or VAR)
	 * @param dimension
	 *            (Scalar = 0, Set = 1, Parameter = 2-20)
	 * @return null if no symbol found
	 */
	public SolutionData getSymbol(String name, String type, int dimension) {
		if (!type.equals(SolutionData.VAR) && !type.equals(SolutionData.EQU)) {
			System.err.println("Unknown symbol type");
			return null;
		}

		if (dimension == 0)
			return getSymbolScalar(name, type);
		else if (dimension > 0 && dimension <= 20)
			return getSymbolND(name, type, dimension);
		else {
			System.err.println("Invalid dimension");
			return null;
		}
	}

	/**
	 * Internal method for extracting 1 - 20 dimension table This method has
	 * been tested only with 1 - 2 dimension table
	 * 
	 * @param name
	 * @param type
	 * @param dimension
	 * @return
	 */

	private SolutionData getSymbolND(String name, String type, int dimension) {

		/*
		 * Each solution data is of type: EQU or VAR Begin with ---- Terminate
		 * with ---- or **** (Summary report) *
		 */
		String regex = String.format(
				"---- %s %s\\s+(.*?)\\n(.*?)(-{4}|\\*{4})", type, name);

		// System.out.println(regex);

		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

		Matcher m = pattern.matcher(text);
		boolean headerFound = false;

		if (!m.find()) {
			System.err.printf("Unable to find symbol :%s %s\n", type, name);
			return null;
		}

		SolutionData data = new SolutionData();
		data.setName(name);
		data.setDescription(m.group(1).trim());
		// System.out.println("Desc: " + data.getDescription());

		data.setDimension(dimension);

		String lines[] = m.group(2).split("\n");

		for (String line : lines) {
			line = line.trim();
			// System.out.println(line);

			if (line.length() == 0)
				continue;

			Matcher headerMatcher = header.matcher(line);
			// check if this is header row
			if (headerMatcher.find()) {
				if (!headerFound) {
					headerFound = true;
					continue;
				} else {
					// encounter header from another param
					break;
				}
			}

			SolutionRow row = new SolutionRow();
			// each index is split by [whitespace]dot
			// System.out.println(line);
			String[] indices = line.split("(\\s+\\.)|[\\s\\.]");
			if (indices.length < dimension) {
				System.err.printf("Dimension mismatch :%s\n", line);
				continue;
			}

			for (int i = 0; i < dimension; i++) {
				// System.out.print(indices[i] + ".");
				row.addIndex(indices[i]);
			}
			// System.out.println();

			// skip n-1 dot
			int valueIndx = -1;
			for (int i = 0; i < dimension - 1; i++) {
				valueIndx = line.indexOf(".", valueIndx + 1);
			}

			// now valueString should contain last index and values
			// after split and ignore the first element,
			// we should get our values
			String valueString = line.substring(valueIndx + 1);

			// System.out.println(valueString);
			valueString = valueString.trim();
			String[] values = valueString.split("\\s+");

			// System.out.print("Value: ");
			// for (int i = 1; i < 5; i++) {
			// System.out.print(values[i] + ":");
			// }
			// System.out.println();
			if (values.length < 5) {
				System.err.println("Parsing error :" + line);
				continue;
			}
			row.setLower(GAMSUtil.parseDouble(values[1]));
			row.setLevel(GAMSUtil.parseDouble(values[2]));
			row.setUpper(GAMSUtil.parseDouble(values[3]));
			row.setMarginal(GAMSUtil.parseDouble(values[4]));

			// System.out.printf("Value: %f %f %f %f \n", row.getLower(),
			// row.getLevel(), row.getUpper(), row.getMarginal());

			data.addRow(row);
		}

		return data;
	}

	private SolutionData getSymbolScalar(String name, String type) {

		/*
		 * Each solution data is of type: EQU or VAR Begin with ---- Terminate
		 * with ---- or **** (Summary report) *
		 */
		String regex = String.format("---- %s %s\\s+(.*?)(-{4}|\\*{4})", type,
				name);

		// System.out.println(regex);

		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

		Matcher m = pattern.matcher(text);

		if (!m.find()) {
			System.err.printf("Unable to find symbol :%s %s\n", type, name);
			return null;
		}

		SolutionData data = new SolutionData();
		data.setName(name);
		data.setDimension(0);

		String lines[] = m.group(1).split("\n");

		// first line is values
		String[] values = lines[0].split("\\s+");
		if (values.length < 4) {
			System.err.println("Parsing error: " + lines[0]);
			return null;
		}

		SolutionRow row = new SolutionRow();
		row.setLower(GAMSUtil.parseDouble(values[0]));
		row.setLevel(GAMSUtil.parseDouble(values[1]));
		row.setUpper(GAMSUtil.parseDouble(values[2]));
		row.setMarginal(GAMSUtil.parseDouble(values[3]));
		data.addRow(row);

		// extract description
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.length() == 0)
				continue;
			if (line.startsWith(name)) {
				// found description line
				String description = line.substring(name.length());
				description = description.trim();
				data.setDescription(description);
				// System.out.println("Desc: " + description);
				break;
			}
		}
		// System.out.printf("Value: %f %f %f %f \n", row.getLower(),
		// row.getLevel(), row.getUpper(), row.getMarginal());

		return data;
	}

	/**
	 * Get objective value
	 * 
	 * @return objective value or Double.NaN if there is parsing error.
	 */
	public Double getObjective() {
		return objective;
	}

	/**
	 * Get solver status text
	 * 
	 * @return
	 */
	public String getSolverStatus() {
		return solverStatus;
	}

	/**
	 * Get solver status code (see GAMS user guide)
	 * 
	 * @return
	 */
	public Integer getSolverStatusCode() {
		return solverStatusCode;
	}

	/**
	 * Model status: eg. OPTIMAL, INFEASIBLE. See GAMS user guide for all
	 * possible values
	 * 
	 * @return GAMS's status text or "PARSING ERROR"
	 */
	public String getModelStatus() {
		return modelStatus;
	}

	/**
	 * Model status: eg. 1 (OPTIMAL), 4 (INFEASIBLE). See GAMS user guide for
	 * all possible values
	 * 
	 * @return GAMS's model status code or 99 for parsing error
	 */
	public Integer getModelStatusCode() {
		return modelStatusCode;
	}

	public static void main(String[] args) {
		FileUtils fileUtils = FileUtils.getInstance(FileUtils.APPLICATION_MODE);
		String results = fileUtils.readFile("resources/solutiondata-test.txt");
		SolutionParser parser = new SolutionParser(results);

		System.out.printf("%d [%s] \n", parser.getSolverStatusCode(),
				parser.getSolverStatus());
		System.out.printf("%d [%s] \n", parser.getModelStatusCode(),
				parser.getModelStatus());
		System.out.printf("%f \n", parser.getObjective());

		// Testing
		SolutionData nut_lo = parser.getSymbol("nut_lo", SolutionData.EQU, 1);
		SolutionData cost = parser.getSymbol("cost", SolutionData.VAR, 0);
		SolutionData xx = parser.getSymbol("xx", SolutionData.VAR, 2);
		SolutionData xxx = parser.getSymbol("xxx", SolutionData.VAR, 3);

		System.out.println("cost :" + cost.getDescription());
		System.out.println("xx " + xx.getDescription());
		for (SolutionRow row : xx.getRows()) {
			System.out.printf("%s %s - %f\n", row.getIndex(0), row.getIndex(1),
					row.getLevel());
		}

		System.out.println("xxx :" + xx.getDescription());
		for (SolutionRow row : xxx.getRows()) {
			if (row.getLevel() > 0)
				System.out.printf("[%s][%s] -> %s\n", row.getIndex(0),
						row.getIndex(1), row.getIndex(2));
		}

	}

}
