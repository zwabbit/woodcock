positive variables
    x(N)    Center coordinates
;

free variable r;


free variables
  y(M,N) Separable points
  r2(M)
;

equations
  inside_socp_2(M)
  def_y(M,N)
  def_r(M)
;

inside_socp_2(M)..
    r2(M) =C= sum(N,y(M,N)) ;

def_y(M,N)..
  y(M,N) =E= x(N) - coord(M,N) ;

def_r(M)..
  r =G= r2(M) ;


model sphere_socp2 /inside_socp_2, def_y, def_r /

solve sphere_socp2 minimizing r using lp;
