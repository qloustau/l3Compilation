import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Edl {

	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;

	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;

	// valeurs possibles du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];

	static int[] transDonDecalage;
	static int[] transCodeDecalage;

	static Descripteur.EltDef[] dicoDef = new Descripteur.EltDef[60];

	static int[][] adFinale = new int[MAXMOD + 1][MAXREF + 1];

	static String[] nomsProgMod = new String[MAXMOD + 1];

	// declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr, nbDef, nbVarGlob;
	static String nomProg;

	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomProg = s;
		nomsProgMod[0] = s;

		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1) + " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				nomsProgMod[nMod] = s;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);
				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
			}

		}
	}

	static void constMap() {
		// f2 = fichier exécutable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "création du fichier " + nomProg + ".map impossible");
		// pour construire le code concaténé de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];
		
		int decalageReserver = 0;
		if(nbVarGlob == 0) {
			decalageReserver = 1;
		}
		
		for (int i = 0; i < nMod + 1; i++) {
			InputStream f = Lecture.ouvrir(nomsProgMod[i] + ".obj");
			if (f == null) {
				System.out.println("fichier " + nomsProgMod[i] + ".obj inexistant");
				System.exit(1);
			}

			List<VecteurTrans> vectTrans = new ArrayList<>();

			for (int j = 0; j < tabDesc[i].getNbTransExt(); j++) {
				vectTrans.add(new VecteurTrans(Lecture.lireInt(f), Lecture.lireInt(f)));
			}

			for (int j = 1; j < tabDesc[i].getTailleCode() + 1; j++) {
				po[j + transCodeDecalage[i] + decalageReserver] = Lecture.lireInt(f);
				ipo++;
			}

			for (VecteurTrans vecteurTrans : vectTrans) {
				//System.out.println(nomsProgMod[i] + " : " + vecteurTrans.adPo + " " + vecteurTrans.code);
				switch (vecteurTrans.code) {
				case TRANSCODE:
					po[vecteurTrans.adPo + transCodeDecalage[i]] += transCodeDecalage[i];
					break;
				case TRANSDON:
					po[vecteurTrans.adPo + transCodeDecalage[i]] += transDonDecalage[i];
					break;
				case REFEXT:
					po[vecteurTrans.adPo + transCodeDecalage[i]] = adFinale[i][po[vecteurTrans.adPo + transCodeDecalage[i]]];
					break;
				default:
					break;
				}
			}

			Lecture.fermer(f);
		}

		po[1] = 1; //produire RESERVER
		po[2] = nbVarGlob;
		
		Ecriture.fermer(f2);
		// création du fichier en mnémonique correspondant
		Mnemo.creerFichier(ipo, po, nomProg + ".ima");
	}

	private static void unionDef(int descIndex) {
		if (tabDesc[descIndex].getNbDef() > MAXREF) {
			erreur(FATALE, "Nombre de points d'entrées trop grand");
		}
		boolean present;
		for (int i = 1; i < tabDesc[descIndex].getNbDef() + 1; i++) {
			present = true;
			Descripteur.EltDef newEltDef = tabDesc[descIndex].new EltDef(tabDesc[descIndex].getDefNomProc(i),
					tabDesc[descIndex].getDefAdPo(i) + transCodeDecalage[descIndex],
					tabDesc[descIndex].getDefNbParam(i));
			for (int j = 0; j < nbDef; j++) {
				// On ajoute que si l'élément n'existe pas dans dicodef
				if (newEltDef.nomProc.equals(dicoDef[j].nomProc))
					present = false;
			}
			if (present)
				dicoDef[nbDef++] = newEltDef;
		}
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;
		nbDef = 0;

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs(); // lecture des descripteurs a completer si besoin

		// On calcule les bases de décalage
		transDonDecalage = new int[nMod + 1];
		transCodeDecalage = new int[nMod + 1];

		// Pas de décalage pour le programme
		transDonDecalage[0] = 0;
		transCodeDecalage[0] = 0;

		int varGCount = tabDesc[0].getTailleGlobaux();
		int tailleCount = tabDesc[0].getTailleCode();
		for (int i = 1; i < nMod + 1; i++) {
			transDonDecalage[i] = varGCount;
			transCodeDecalage[i] = tailleCount;

			varGCount += tabDesc[i].getTailleGlobaux();
			tailleCount += tabDesc[i].getTailleCode();
		}
		nbVarGlob = varGCount;

		// On construit dicoDef
		for (int i = 0; i < nMod + 1; i++) {
			unionDef(i);
		}

		// for (Descripteur.EltDef eltDef : dicoDef) {
		// if(eltDef != null)
		// System.out.println(eltDef.nomProc + " " + eltDef.adPo + " " +
		// eltDef.nbParam);
		// }

		// On construit adFinale

		for (int i = 0; i < nMod + 1; i++) {
			for (int k = 1; k < tabDesc[i].getNbRef() + 1; k++) {
				int j = 0;
				adFinale[i][k] = -1;
				while (j < nbDef && adFinale[i][k] == -1) {
					if (tabDesc[i].getRefNomProc(k).equals(dicoDef[j].nomProc)) {
						adFinale[i][k] = dicoDef[j].adPo;
					}
					j++;
				}
				if (adFinale[i][k] == -1) {
					erreur(FATALE, "Procedure référée non existante");
				}
			}
		}

		// for (int i = 0; i < nMod + 1; i++) {
		// for (int j = 0; j < nbDef; j++) {
		// System.out.println("i " + i +" "+ adFinale[i][j]);
		// }
		// }

		if (nbErr > 0) {
			System.out.println("programme exécutable non produit");
			System.exit(1);
		}

		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap(); // a completer
		System.out.println("Edition de liens terminee");
	}
}

class VecteurTrans {
	public int adPo;
	public int code;

	public VecteurTrans(int adPo, int code) {
		this.adPo = adPo;
		this.code = code;
	}
}