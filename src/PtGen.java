/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libclass) *
 * complement Ã  l'ANALYSEUR LEXICAL produit par ANTLR * * * nom du programme
 * compile, sans suffixe : String UtilLex.nomSource * ------------------------ *
 * * attributs lexicaux (selon items figurant dans la grammaire): *
 * ------------------ * int UtilLex.valNb = valeur du dernier nombre entier lu
 * (item nbentier) * int UtilLex.numId = code du dernier identificateur lu (item
 * ident) * * * methodes utiles : * --------------- * void
 * UtilLex.messErr(String m) affichage de m et arret compilation * String
 * UtilLex.repId(int nId) delivre l'ident de codage nId * void afftabSymb()
 * affiche la table des symboles *
 *********************************************************************************/

// classe de mise en oeuvre du compilateur
// =======================================
// (verifications semantiques + production du code objet)

public class PtGen {

  // constantes manipulees par le compilateur
  // ----------------------------------------

  private static final int

  // taille max de la table des symboles
  MAXSYMB = 300,

      // codes MAPILE :
      AREMPLIR = -1, RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4, OU = 5, ET = 6, NON = 7, INF = 8,
      INFEG = 9, SUP = 10, SUPEG = 11, EG = 12, DIFF = 13, ADD = 14, SOUS = 15, MUL = 16, DIV = 17, BSIFAUX = 18,
      BINCOND = 19, LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23, ARRET = 24, EMPILERADG = 25, EMPILERADL = 26,
      CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30,

      // codes des valeurs vrai/faux
      VRAI = 1, FAUX = 0,

      // types permis :
      ENT = 1, BOOL = 2, NEUTRE = 3,

      // catï¿½gories possibles des identificateurs :
      CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8, PRIVEE = 9,

      // valeurs possible du vecteur de translation
      TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

  // utilitaires de controle de type
  // -------------------------------

  private static void verifEnt() {
    if (tCour != ENT) {
      UtilLex.messErr("expression entiere attendue");
    }
  }

  private static void verifBool() {
    if (tCour != BOOL) {
      UtilLex.messErr("expression booleenne attendue");
    }
  }

  // pile pour gerer les chaines de reprise et les branchements en avant
  // -------------------------------------------------------------------

  private static TPileRep pileRep;

  // production du code objet en memoire
  // -----------------------------------

  private static ProgObjet po;

  // COMPILATION SEPAREE
  // -------------------
  //
  // modification du vecteur de translation associe au code produit
  // + incrementation attribut nbTransExt du descripteur
  // NB: effectue uniquement si c'est une reference externe ou si on compile
  // un
  // module
  private static void modifVecteurTrans(int valeur) {
    if (valeur == REFEXT || desc.getUnite().equals("module")) {
      po.vecteurTrans(valeur);
      desc.incrNbTansExt();
    }
  }

  // descripteur associe a un programme objet
  private static Descripteur desc;

  // autres variables fournies
  // -------------------------
  public static String trinome = "Thomas Foucault, Alexandre Moriniaux, Nathanaël Touchard"; // MERCI
  // de
  // renseigner
  // ici
  // un
  // nom
  // pour
  // le
  // trinome,
  // constitue
  // de
  // exclusivement
  // de
  // lettres

  private static int tCour; // type de l'expression compilee
  private static int vCour; // valeur de l'expression compilee le cas echeant

  private static int varGlbIt; // iterateur pour remplir tabSymb avec les var
  // globales
  private static int nbVarAReserver; // nombre de var à réserver lors d'une
  // déclaration

  private static int varIdent;
  private static int varType;
  private static int varCategorie;

  // Dï¿½finition de la table des symboles
  //
  private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

  // it = indice de remplissage de tabSymb
  // bc = bloc courant (=1 si le bloc courant est le programme principal)
  private static int it, bc;

  // utilitaire de recherche de l'ident courant (ayant pour code
  // UtilLex.numId)
  // dans tabSymb
  // rend en resultat l'indice de cet ident dans tabSymb (O si absence)
  private static int presentIdent(int binf) {
    int i = it;
    while (i >= binf && tabSymb[i].code != UtilLex.numId) {
      i--;
    }
    if (i >= binf) {
      return i;
    } else {
      return 0;
    }
  }

  // utilitaire de placement des caracteristiques d'un nouvel ident dans
  // tabSymb
  //
  private static void placeIdent(int c, int cat, int t, int v) {
    if (it == MAXSYMB) {
      UtilLex.messErr("debordement de la table des symboles");
    }
    it = it + 1;
    tabSymb[it] = new EltTabSymb(c, cat, t, v);
    if (cat == VARGLOBALE) {
      varGlbIt++;
    }
  }

  // utilitaire d'affichage de la table des symboles
  //
  private static void afftabSymb() {
    System.out.println("       code           categorie      type    info");
    System.out.println("      |--------------|--------------|-------|----");
    for (int i = 1; i <= it; i++) {
      if (i == bc) {
        System.out.print("bc=");
        Ecriture.ecrireInt(i, 3);
      } else if (i == it) {
        System.out.print("it=");
        Ecriture.ecrireInt(i, 3);
      } else {
        Ecriture.ecrireInt(i, 6);
      }
      if (tabSymb[i] == null) {
        System.out.println(" rï¿½fï¿½rence NULL");
      } else {
        System.out.println(" " + tabSymb[i]);
      }
    }
    System.out.println();
  }

  // initialisations A COMPLETER SI BESOIN
  // -------------------------------------

  public static void initialisations() {

    // indices de gestion de la table des symboles
    it = 0;
    bc = 1;

    // pile des reprises pour compilation des branchements en avant
    pileRep = new TPileRep();
    // programme objet = code Mapile de l'unite en cours de compilation
    po = new ProgObjet();
    // COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
    desc = new Descripteur();

    // initialisation necessaire aux attributs lexicaux (quand enchainement
    // de
    // compilations)
    UtilLex.initialisation();

    // initialisation du type de l'expression courante
    tCour = NEUTRE;

    varGlbIt = 0;
    nbVarAReserver = 0;

    varIdent = 0;
    varType = 0;
    varCategorie = 0;

  } // initialisations

  // code des points de generation A COMPLETER
  // -----------------------------------------
  public static void pt(int numGen) {
    // System.out.println("PTGEN " + numGen);

    switch (numGen / 100) {
    case 1:
      ptNathanael(numGen);
      return;
    case 2:
      ptAlexandre(numGen);
      return;
    case 3:
      ptThomas(numGen);
      return;
    }

    switch (numGen) {
    case 0:
      initialisations();
      break;
    case 1: // add const
      placeIdent(UtilLex.numId, CONSTANTE, tCour, vCour);
      // afftabSymb();
      break;
    case 2: // val nbentier pos
      tCour = ENT;
      vCour = UtilLex.valNb;
      break;
    case 3: // val nbentier neg
      tCour = ENT;
      vCour = -UtilLex.valNb;
      break;
    case 4: // val vrai
      tCour = BOOL;
      vCour = VRAI;
      break;
    case 5: // val faux
      tCour = BOOL;
      vCour = FAUX;
      break;
    case 6: // type ENT
      tCour = ENT;
      break;
    case 7: // type BOOL
      tCour = BOOL;
      break;
    case 8: // decl var
      placeIdent(UtilLex.numId, VARGLOBALE, tCour, varGlbIt);
      nbVarAReserver++;
      // afftabSymb();
      break;
    case 10: // reserver var
      po.produire(RESERVER); // reserver
      po.produire(nbVarAReserver);
      nbVarAReserver = 0;
      break;
    case 13: // primaire valeur
      po.produire(EMPILER); // empiler val
      po.produire(UtilLex.valNb); // valeur
      break;
    case 14: // primaire ident
      int ident = presentIdent(1);
      if (ident != 0) {
        tCour = tabSymb[ident].type;
        switch (tabSymb[ident].categorie) {
        case CONSTANTE:
          po.produire(EMPILER); // empiler
          break;
        case VARGLOBALE:
          po.produire(CONTENUG); // contenug
          break;
        default:
          break;
        }
        po.produire(tabSymb[ident].info);
      } else {
        UtilLex.messErr("ident inexistant");
      }
      break;
    case 15:
      verifEnt();
      break;
    case 16:
      verifBool();
      break;
    case 17: // mul
      po.produire(MUL);
      break;
    case 18: // div
      po.produire(DIV);
      break;
    case 19: // +
      po.produire(ADD);
      break;
    case 20: // -
      po.produire(SOUS);
      break;
    case 21: // =
      po.produire(EG);
      break;
    case 22: // <>
      po.produire(DIFF);
      break;
    case 23: // >
      po.produire(SUP);
      break;
    case 24: // >=
      po.produire(SUPEG);
      break;
    case 25: // <
      po.produire(INF);
      break;
    case 26: // <=
      po.produire(INFEG);
      break;
    case 27: // non
      po.produire(NON);
      break;
    case 28: // et
      po.produire(ET);
      break;
    case 29: // ou
      po.produire(OU);
      break;
    case 30: // apres operation binaire int int -> bool
      tCour = BOOL;
      break;
    default:
      System.out.println("Point de generation non prevu dans votre liste");
      break;
    }
  }

  private static void ptNathanael(int numGen) {
    switch (numGen) {
    case 100: // Lecture de l'ident
      varIdent = presentIdent(1);
      if (varIdent == 0) {
        UtilLex.messErr("Identifiant inconnu");
      }
      if (tabSymb[varIdent].categorie == CONSTANTE) {
        UtilLex.messErr("L'identifiant n'est pas une variable");
      }

      tCour = tabSymb[varIdent].type;
      varType = tabSymb[varIdent].type;
      varCategorie = tabSymb[varIdent].categorie;
      break;

    case 101:// affectation
      // verifier le type
      if (varType == ENT) {
        verifEnt();
      } else {
        verifBool();
      }

      // affecter
      switch (varCategorie) {

      case VARGLOBALE:
        po.produire(AFFECTERG);
        po.produire(tabSymb[varIdent].info);
        break;

      default:
        UtilLex.messErr("L'affectation est impossible");
        break;
      }
      break;

    case 102:
      pileRep.empiler(po.getIpo() + 1);
      break;

    case 103: // "ttq faire" production bsifaux ?, empiler l'adresse dans
              // pileRep
      po.produire(BSIFAUX);
      po.produire(AREMPLIR);
      pileRep.empiler(po.getIpo());
      break;

    case 104: // "fait" résolution du bsifaux (ttq)
      // depiler pileRep pour résoudre le bsifaux
      po.produire(BINCOND);
      po.modifier(pileRep.depiler(), po.getIpo() + 2);
      po.produire(pileRep.depiler());
      break;

    case 105:// "cond" on empile 0 en premier
      pileRep.empiler(0);
      break;
    case 106: // cond production bsifaux
      po.produire(BSIFAUX);
      po.produire(AREMPLIR);
      pileRep.empiler(po.getIpo());
      break;
    case 107:// cond production bincond chaine resoud bsifaux
      po.produire(BINCOND);

      po.modifier(pileRep.depiler(), po.getIpo() + 2);
      po.produire(pileRep.depiler());

      pileRep.empiler(po.getIpo());

      break;
    case 108: // cond resolution dernier bsifaux si pas d'instruction "aut"
      po.modifier(pileRep.depiler(), po.getIpo() + 1);
      break;
    case 109:
      int tmp = 0;
      int firstBincond = pileRep.depiler();
      int next = po.getElt(firstBincond);

      po.modifier(firstBincond, po.getIpo() + 1);

      while (next != 0) {
        tmp = po.getElt(next);
        po.modifier(next, po.getIpo() + 1);
        next = tmp;
      }

      break;
    default:
      System.out.println("Point de generation non prevu dans votre liste");
      break;
    }

  }

  private static void ptAlexandre(int numGen) {
    switch (numGen) {
    case 200: // lecture
      int identLecture = presentIdent(1);
      if (identLecture == 0) {
        UtilLex.messErr("Identifiant inconnu");
      }
      if (tabSymb[identLecture].type == ENT) {
        po.produire(LIRENT);
        po.produire(AFFECTERG);
        po.produire(tabSymb[identLecture].info);
      }
      if (tabSymb[identLecture].type == BOOL) {
        po.produire(LIREBOOL);
        po.produire(AFFECTERG);
        po.produire(tabSymb[identLecture].info);
      }
      break;
    case 201: // ecriture
      if (tCour == ENT) {
        po.produire(ECRENT);
      }
      if (tCour == BOOL) {
        po.produire(ECRBOOL);
      }
      break;

    default:
      System.out.println("Point de generation non prevu dans votre liste");
      break;
    }
  }

  private static void ptThomas(int numGen) {
    switch (numGen) {
    case 300: // "si alors" production bsifaux ?, empiler l'adresse dans
      // pilerep
      po.produire(BSIFAUX);
      po.produire(AREMPLIR);
      pileRep.empiler(po.getIpo());
      break;
    case 301: // "sinon" production bincond ?, empiler l'adresse dans
      // pilerep,
      // résoudre bsifaux du si
      po.produire(BINCOND);

      po.produire(AREMPLIR);

      // depiler pilerep pour résoudre le bsifaux du si
      po.modifier(pileRep.depiler(), po.getIpo() + 1);

      pileRep.empiler(po.getIpo());
      break;
    case 302: // "fsi" résolution des bincond et bsifaux (sinon et si)
      // depiler pilerep pour résoudre les bincond et bsifaux est
      // conditions
      po.modifier(pileRep.depiler(), po.getIpo() + 1);
      break;
    case 303: // fin du programme
      po.produire(ARRET);
      po.constGen();
      po.constObj();
      break;
    default:
      System.out.println("Point de generation non prevu dans votre liste");
      break;

    }
  }
}
