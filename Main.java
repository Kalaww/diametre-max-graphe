/*
	Cédric DESGRANGES - L3 INFO 3
 */
import java.util.HashSet;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.LinkedList;

import java.io.File;

/**
 * Main : contient les fonctions de calcul d'excentricité et de lecture de fichier contenant le graphe
 */
class Main{
	
	public static void main(String args[]){
		if(args.length != 2){
			usage();
			return;
		}
		
		if(args[0].length() == 1 && args[0].charAt(0) == 'e'){
			Graphe g = readFile(args[1]);
			System.out.print(excentriciteExacte(g));
		}else if(args[0].length() == 1 && args[0].charAt(0) == 'h'){
			Graphe g = readFile(args[1]);
			System.out.print(excentriciteHabib(g));
		}else{
			System.out.println("Option inconnue: "+args[0]);
			usage();
		}
	}

	/**
	 * Usage de l'invite de commande
	 */
	static void usage(){
		System.out.println("usage: java Main [e,h] [nom fichier au format pfg]");
	}

	/**
	 * Ouvre un fichier et creer la structure du graphe qu'il contient
	 * @param  nom nom du fichier
	 * @return la strucuture du graphe
	 */
	static Graphe readFile(String nom){
		Scanner sc = null;
		try{
			sc = new Scanner(new File(nom));
		} catch(Exception e){
			exit("Impossible d'ouvrir le fichier "+nom, 1);
		}
		
		String ligne = sc.nextLine();
		if(ligne == null || ligne. length() == 0){
			exit("Le fichier "+nom+" n'est pas au bon format", 1);
		}
		int n = Integer.parseInt(ligne);
		Graphe g = new Graphe(n);
		
		
		while(sc.hasNextLine()){
			ligne = sc.nextLine();
			
			if(ligne != null){
				String[] ligneSplit = ligne.split(" ");
				if(ligneSplit.length != 2){
					exit("Le fichier "+nom+" n'est pas au bon format", 1);
				}
				g.addArete(Integer.parseInt(ligneSplit[0]), Integer.parseInt(ligneSplit[1]));
			}else if(ligne.length() == 0) break;
			else exit("Le fichier "+nom+" n'est pas au bon format", 1);
		}
		return g;
	}
	
	/**
	 * Affiche un message et termine le programme
	 * @param m message
	 * @param v valeur de exit
	 */
	static void exit(String m, int v){
		System.out.println(m);
		if(v >= 0) System.exit(v);
	}
	
	/**
	 * Excentricité exacte d'un graphe
	 * @param  g graphe
	 * @return l'excentricité du graphe
	 */
	static int excentriciteExacte(Graphe g){
		int[] distances = new int[g.noeuds.length];
		LinkedList<Noeud> file = new LinkedList<Noeud>();
		int max = 1;

		for(Noeud noeud : g.noeuds){
			file.clear();
			
			for(int i = 0; i < distances.length; i++)
				distances[i] = 0;
			
			file.addLast(noeud);
			noeud.couleur = Noeud.Couleur.NOIR;
			while(!file.isEmpty()){
				Noeud courant = file.removeFirst();
				for(Noeud voisin : courant.voisins){
					if(voisin.couleur != Noeud.Couleur.NOIR){
						int d = distances[courant.valeur-1]+1;
						if(distances[voisin.valeur-1] == 0 || distances[voisin.valeur-1] > d){
							distances[voisin.valeur-1] = d;
						}
						file.addLast(voisin);
						voisin.couleur = Noeud.Couleur.NOIR;
					}
				}
			}

			for(int i = 0; i < distances.length; i++){
				if(distances[i] > max) max = distances[i];
			}
			g.allBlanc();
		}
		return max;
	}

	/**
	 * Excentricité d'un noeud
	 * @param  g graphe
	 * @param  n le noeud sur lequel on démarre le parcours en largeur
	 * @return un couple noeud et distance correspondant au noeud du plus long chemin en partant de n
	 */
	private static Entry<Noeud, Integer> excentriciteNoeud(Graphe g, Noeud n){
		LinkedList<Noeud> file = new LinkedList<Noeud>();
		HashMap<Noeud, Integer> map = new HashMap<Noeud, Integer>();
		Entry<Noeud, Integer> max = null;

		g.allBlanc();
		for(Noeud k : g.noeuds) map.put(k, new Integer(0));

		file.addLast(n);
		n.couleur = Noeud.Couleur.NOIR;
		while(!file.isEmpty()){
			Noeud courant = file.removeFirst();
			for(Noeud voisin : courant.voisins){
				if(voisin.couleur != Noeud.Couleur.NOIR){
					Integer d = map.get(courant);
					d = new Integer(d.intValue()+1);
					if(map.get(voisin).intValue() == 0 || map.get(voisin).intValue() > d){
						map.remove(voisin);
						map.put(voisin, d);
					}
					file.addLast(voisin);
					voisin.couleur = Noeud.Couleur.NOIR;
				}
			}
		}

		Entry<Noeud, Integer> tmpEntry = null;
		for(Entry<Noeud, Integer> k : map.entrySet()){
			if(max == null || k.getValue().intValue() > max.getValue().intValue()){
				max = k;
			}
		}
		return max;
	}

	/**
	 * Excentricité selon l'algorithme de Habib
	 * @param  g graphe
	 * @return l'excentricité du graphe
	 */
	@SuppressWarnings("unchecked")
	static int excentriciteHabib(Graphe g){
		LinkedList<Noeud> file = new LinkedList<Noeud>();
		HashMap<Noeud, Integer> map = new HashMap<Noeud, Integer>();
		HashMap<Noeud, LinkedList<Integer>> mapPile = new HashMap<Noeud, LinkedList<Integer>>(); 
		Noeud x2 = null, x3 = null, x4 = null, x5 = null;

		// calcul de x2
		x2 = excentriciteNoeud(g, g.noeuds[0]).getKey();

		//calcul de x3
		g.allBlanc();

		for(Noeud n : g.noeuds){
			map.put(n, new Integer(0));
			mapPile.put(n, new LinkedList<Integer>());
		}

		file.addLast(x2);
		x2.couleur = Noeud.Couleur.NOIR;
		while(!file.isEmpty()){
			Noeud courant = file.removeFirst();
			for(Noeud voisin : courant.voisins){
				if(voisin.couleur != Noeud.Couleur.NOIR){
					Integer d = map.get(courant);
					d = new Integer(d.intValue()+1);
					if(map.get(voisin).intValue() == 0 || map.get(voisin).intValue() > d){
						map.remove(voisin);
						map.put(voisin, d);
						LinkedList<Integer> tmpPile = (LinkedList<Integer>)mapPile.get(courant).clone();
						tmpPile.addLast(new Integer(voisin.valeur));
						mapPile.remove(voisin);
						mapPile.put(voisin, tmpPile);
					}
					file.addLast(voisin);
					voisin.couleur = Noeud.Couleur.NOIR;
				}
			}
		}

		Noeud noeudMax = null;
		int valeurMax = 0, valeur = 0;
		for(Noeud n : g.noeuds){
			valeur = map.get(n).intValue();
			if(valeur > valeurMax){
				valeurMax = valeur;
				noeudMax = n;
			}
		}

		x3 = noeudMax;
		LinkedList<Integer> x3Pile = mapPile.get(x3);
		if(x3Pile.size() == 1) x4 = g.noeuds[x3Pile.get(0)-1];
		else x4 = g.noeuds[x3Pile.get(x3Pile.size()/2-1)-1];


		// calcul de x5
		x5 = excentriciteNoeud(g, x4).getKey();

		//calcul de x6
		return excentriciteNoeud(g, x5).getValue().intValue();
	}
}


/**
 * Structure de graphe
 */
class Graphe{

	/**
	 * Tableau des noeuds.
	 * Le noeud de valeur n se trouve dans la case d'index n-1
	 */
	Noeud[] noeuds;
	
	/**
	 * Constructeur
	 * @param  n nombre de sommet dans le graphe
	 */
	public Graphe(int n){
		noeuds = new Noeud[n];
		for(int i = 1; i <= n; i++){
			noeuds[i-1] = new Noeud(i);
		}
	}
	
	/**
	 * Nombre de sommet du graphe
	 * @return nombre de sommet du graphe
	 */
	public int taille(){
		return noeuds.length;
	}
	
	/**
	 * Ajoute une arête au graphe entre les sommets n et v
	 * @param n sommet adjacent à l'arête
	 * @param v sommet adjacent à l'arête
	 */
	public void addArete(int n, int v){
		if(n > taille() || n < 1){
			Main.exit("Sommet "+n+" introuvable", 1);
		}
		
		if(v > taille() || v < 1){
			Main.exit("Sommet "+v+" introuvable", 1);
		}
		noeuds[n-1].voisins.add(noeuds[v-1]);
		noeuds[v-1].voisins.add(noeuds[n-1]);
	}
	
	/**
	 * Passe tous les sommets du graphe à blanc
	 */
	public void allBlanc(){
		for(Noeud n : noeuds)
			n.couleur = Noeud.Couleur.BLANC;
	}
}


/**
 * Structure de sommet pour le graphe
 */
class Noeud {
		
	/**
	 * Valeur du noeud
	 */
	int valeur;
	
	/**
	 * Ensemble des voisins du sommet
	 */
	HashSet<Noeud> voisins;
	
	/**
	 * Enum pour les couleurs possibles d'un sommet
	 */
	enum Couleur{BLANC, NOIR};
	
	/**
	 * Couleur du sommet pour le parcour en largeur
	 */
	Couleur couleur;
	
	/**
	 * Constructeur
	 * @param  valeur valeur du noeud
	 */
	public Noeud(int valeur){
		this.valeur = valeur;
		voisins = new HashSet<Noeud>();
		couleur = Couleur.BLANC;
		}
	
	public int hashCode(){
		return valeur;
	}

	public String toString(){
		return ""+valeur;
	}
}