// Grammaire du langage PROJET
// COMP L3  
// Anne Grazon, Veronique Masson
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog  EOF {PtGen.pt(303);}
      |    unitmodule  EOF {PtGen.pt(303);}
  ;
  
unitprog
  : 'programme' {PtGen.pt(315);} ident ':'  
     declarations  
     corps { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' {PtGen.pt(316);} ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident {PtGen.pt(110);}(',' ident {PtGen.pt(110);})* ptvg
  ;
  
partieref: 'ref'  specif (',' specif )* ptvg
  ;
  
specif  : ident {PtGen.pt(114);} ( 'fixe' '(' type  {PtGen.pt(112);}( ',' type  {PtGen.pt(112);})* ')' )? 
                 ( 'mod'  '(' type  {PtGen.pt(113);}( ',' type  {PtGen.pt(113);})* ')' )? {PtGen.pt(111);}
  ;
  
consts  : 'const' ( ident  '=' valeur  ptvg {PtGen.pt(1);} )+ 
  ;
  
vars  : 'var' ( type ident {PtGen.pt(8);} ( ','  ident {PtGen.pt(8);} )* ptvg )+ {PtGen.pt(10);}
  ;
  
type  : 'ent' {PtGen.pt(6);}
  |     'bool' {PtGen.pt(7);}
  ;
  
decprocs: {PtGen.pt(309);} (decproc ptvg)+ {PtGen.pt(310);}
  ;
  
decproc :  'proc'  ident {PtGen.pt(304);} parfixe? parmod? {PtGen.pt(307);} consts? vars? corps 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' {PtGen.pt(308);}
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident {PtGen.pt(305);} ( ',' ident {PtGen.pt(305);} )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(306);} ( ',' ident {PtGen.pt(306);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression {PtGen.pt(300);} 'alors' instructions ('sinon' {PtGen.pt(301);} instructions)? 'fsi' {PtGen.pt(302);}
  ;
  
inscond : 'cond' {PtGen.pt(105);} expression {PtGen.pt(106);} ':' instructions 
          (',' {PtGen.pt(107);} expression {PtGen.pt(106);} ':' instructions )* 
          ('aut' {PtGen.pt(107);} instructions | {PtGen.pt(108);} ) 
          'fcond' {PtGen.pt(109);}
  ;
  
boucle  : 'ttq' {PtGen.pt(102);}  expression {PtGen.pt(103);} 'faire' instructions  'fait' {PtGen.pt(104);}
  ;
  
lecture: 'lire' '(' ident{PtGen.pt(200);}  ( ',' ident{PtGen.pt(200);}  )* ')'
  ;

ecriture: 'ecrire' '(' expression{PtGen.pt(201);}  ( ',' expression{PtGen.pt(201);}  )* ')'
   ;

affouappel
  : ident  (  {PtGen.pt(100);}  ':=' expression {PtGen.pt(101);} 
            | {PtGen.pt(311);}  (effixes (effmods)? )?  {PtGen.pt(314);}
           )
  ;
  
effixes : '(' (expression {PtGen.pt(312);} (',' expression {PtGen.pt(312);} )*)? ')'
  ;
  
effmods :'(' (ident {PtGen.pt(313);} (',' ident {PtGen.pt(313);} )*)? ')'
  ; 
  
expression: (exp1) ('ou' {PtGen.pt(16);} exp1 {PtGen.pt(16);} {PtGen.pt(29);} )*
  ;
  
exp1  : exp2 ('et' {PtGen.pt(16);} exp2 {PtGen.pt(16);} {PtGen.pt(28);} )*
  ;
  
exp2  : 'non' exp2 {PtGen.pt(16);} {PtGen.pt(27);}
  | exp3  
  ;
  
exp3  : exp4 
  ( '=' {PtGen.pt(15);}  exp4 {PtGen.pt(15);} {PtGen.pt(21);} {PtGen.pt(30);}
  | '<>' {PtGen.pt(15);} exp4 {PtGen.pt(15);} {PtGen.pt(22);} {PtGen.pt(30);}
  | '>'  {PtGen.pt(15);} exp4 {PtGen.pt(15);} {PtGen.pt(23);} {PtGen.pt(30);}
  | '>=' {PtGen.pt(15);} exp4 {PtGen.pt(15);} {PtGen.pt(24);} {PtGen.pt(30);}
  | '<'  {PtGen.pt(15);} exp4 {PtGen.pt(15);} {PtGen.pt(25);} {PtGen.pt(30);}
  | '<=' {PtGen.pt(15);} exp4 {PtGen.pt(15);} {PtGen.pt(26);} {PtGen.pt(30);}
  ) ?
  ;
  
exp4  : exp5 
        ('+' {PtGen.pt(15);} exp5 {PtGen.pt(15);} {PtGen.pt(19);}
        |'-' {PtGen.pt(15);} exp5 {PtGen.pt(15);} {PtGen.pt(20);}
        )*
  ;
  
exp5  : primaire 
        (    '*' {PtGen.pt(15);}  primaire {PtGen.pt(15);} {PtGen.pt(17);}
          | 'div' {PtGen.pt(15);} primaire {PtGen.pt(15);} {PtGen.pt(18);}
        )*
  ;
  
primaire: valeur {PtGen.pt(13);}
  | ident {PtGen.pt(14);}
  | '(' expression ')'
  ;
  
valeur  : nbentier {PtGen.pt(2);}
  | '+' nbentier {PtGen.pt(2);}
  | '-' nbentier {PtGen.pt(3);}
  | 'vrai' {PtGen.pt(4);}
  | 'faux' {PtGen.pt(5);}
  ;

// partie lexicale  : cette partie ne doit pas etre modifie  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valNb = Integer.parseInt($INT.text);}; // mise a jour de valNb

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numId
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // la table des symboles n'est pas geree au niveau lexical
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes



COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	   