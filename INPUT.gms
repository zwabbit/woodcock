free variable totalCost;

binary variable cut(x, y);
positive variable subsidy(x, y);

equations
balance_val(x,y),
balance_cuts,
*isolate(x,y),
calc_cost;

balance_val(xSub,ySub)..
subsidy(xSub,ySub) + patchValue(xSub, ySub) =g= minVal * cut(xSub,ySub);

balance_cuts..
sum((xSub,ySub), cut(xSub,ySub)) =e= requiredPatches;

*isolate(xSub,ySub)..
*cut(xSub-1,ySub-1)$((ord(xSub) > 1) and (ord(ySub) > 1)) +
*cut(xSub,ySub-1)$(ord(ySub) > 1) +
*cut(xSub+1,ySub-1)$(ord(xSub) <= card(xSub) and ord(ySub) > 1) +
*cut(xSub-1,ySub)$(ord(xSub) > 1) +
*cut(xSub+1,ySub)$(ord(xSub) <= card(xSub)) +
*cut(xSub-1,ySub+1)$(ord(xSub) > 1 and ord(ySub) <= card(ySub)) +
*cut(xSub,ySub+1)$(ord(ySub) <= card(ySub)) +
*cut(xSub+1,ySub+1)$(ord(xSub) <= card(xSub) and ord(ySub) <= card(ySub))
*=l= 3;

calc_cost..
sum((xSub,ySub), subsidy(xSub,ySub)) =e= totalCost;

model optCut /all/;

solve optCut using mip minimizing totalCost;