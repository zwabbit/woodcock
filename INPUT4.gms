*set M patches /willfillindynamically/;
*set N dimensions /0,1/;

*parameter subsidy(M);
*parameter p(M,N) patches

binary variable selected(M);

free variables
subsidy_cost;
;

equations
count_lower_bound,
calc_val
;

count_lower_bound..
sum(M, selected(M)) =e= requiredPatches;

calc_val..
sum(M, subsidy(M) * selected(M)) =e= subsidy_cost;

model cost_model /count_lower_bound,calc_val/;

solve cost_model maximizing subsidy_cost using mip;

scalar total_selected;
total_selected = sum(M, selected.l(M));
display total_selected;
