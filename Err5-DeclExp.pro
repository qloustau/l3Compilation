programme premiertest:	{erreur testee: ident non declare}

const min=7; max=+77; marq=-1; oui=vrai; nenni=faux;
var ent i,j;
    bool b1,b2,b3;

debut

	i:= (x-min) div 2;	{x: ident non declare}
	b1:= oui et nenni;
	b2:= non b1 et (oui ou nenni);
	j:= (i+5)*10;
	b3:= (i<=j) ou (i<>10);
	ecrire(,j);
	ecrire(b1,b2,b3);
fin
