alias(M,A);
alias(Y,J);

*parameter coord(M,N);

free variable objective;

binary variable selected(M);

positive variables
d_total,
d(M,A,N),
dp(M,A,N),
dn(M,A,N),
subsidy_total
;

equations
balance_d(M,A,N),
calc_dist(M,A,N),
sum_d,
sum_subsidy,
objective_function
;

balance_d(M,A,N)..
dp(M,A,N) + dn(M,A,N) =e= d(M,A,N);

calc_x_dist(M,A,N)..
selected(M) * coord(M,N) - selected(A) * coord(A,N) =e= dp(M,A,N) - dn(M,A,N);

sum_d..
sum((M,A,N), d(M,A,N)) =e= d_total;

sum_subsidy..
sum(M, subsidy(M) * selected(M)) =e= subsidy_total;

objective_function..
objective =e= subsidy_total + d_total;

model input_three /all/;

solve input_three minimizing objective using mip;
