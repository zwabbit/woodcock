*set M patches /willfillindynamically/;
*set N dimensions /0,1/;

*parameter subsidy(M);
*parameter p(M,N) patches

binary variable selected(M);

free variables
y(M,N) Separable points,
r2(M),
r,
obj_val;
;

positive variables
x(N) center coordinates,
subsidy_cost;

equations
inside_socp_2(M),
def_y(M,N),
def_r(M),
count_lower_bound,
calc_val
;

def_y(M,N)..
y(M,N) =E= x(N) - p(M,N) * selected(M);

def_r(M)..
r =G= r2(M);

inside_socp_2(M)..
r2(M) =C= sum(N,y(M,N));

counter_lower_bound..
sum(M, selected(M)) =g= requiredPatches;

calc_val..
sum(M, subsidy(M) * selected(M)) =e= subsidy_cost;

equations
cost_obj,
dist_obj;

cost_obj..
obj_val =e= subsidy_cost;

dist_obj..
obj_val =e= r;

model sphere_socp2 /inside_socp_2(M),def_y(M,N),def_r,count_lower_bound,dist_obj/;

model cost_model /inside_socp_2(M),def_y(M,N),def_r,count_lower_bound,cost_obj/;

option mip=mosek;

solve sphere_socp2 minimizing obj_val using mip;

r.up = 1.01 * r.l;

solve cost_model maximizing obj_val using mip;
