Ghostlab : Groupe 59

Nous avons réaliser le jeu Ghostlab uniquement en Java, notre jeu implemente toute les fonctionnalité qui sont demander dans le sujet.

Compiler : "javac *.java"
Executer :
	- Serveur :  "java Serveur"
	- Client : "java Client"
	
Pour pouvoir jouer, il suffit d'écrire le message que l'on veut envoyer au serveur dans le terminal du client, et ensuite le serveur répondra dans le meme terminal sa réponse à la requete du client.


Notre code est divisé en 6 classes :

	- La classe Serveur : cette classe attend une connexion d'un Client, et quand il est connecter, le serveur crée un thread avec un objet de type Connexion qui est une classe interne static de la classe Serveur qui gère toutes les requetes que peut faire le Client
	- La classe Client : se connecte sur le port ou est lancé le serveur, puis attends la requete de l'utilisateur pour l'envoyer ensuite au serveur, ensuite reçoit la reponse du serveur et l'affiche, possede une classe interne static pour recevoir les message multidiffusé et qui possede elle meme une classe interne static pour recevoir les message UDP.
	- La classe Partie : cette classe implements Runnable, pour chaque Partie on crée un thread, la methode run de la classe Partie sert principalement à voir les different états de la partie (en attente, en cours, terminé) et a la fin multidiffuse le message ENDGA avec le nom du gagnant. Le déplacement des joueurs est géré dans la classe Serveur et le déplacement des fantomes est géré dans la classe Fantome. Chaque partie a son propre labyrinthe (classe Labyrinthe), sa propre ArrayList de joueur qui correspond au joueur present dans la partie, et sa propre adresse et port de diffusion.
	- La classe Labyrinthe : est un tableau de tableau de caractère, 'V' pour une case vide, 'F' pour une case ou il y a un fantome, '|' pour une case ou il y a un mur, chaque labyrinthe a sa propre ArrayList de Fantome
	- La classe Fantome : cette classe implements Runnable, les fantomes sont créé quand la partie est créée, mais on démarre les thread des fantomes quand la partie démarre, ensuite toutes les x secondes le fantome se déplace et multidiffuse sa nouvelle position (10 seconde < x < 20 seconde). Une fois que le fantome est attrapé, son thread se termine et il est supprimer de la liste des fantome dans le labyrinthe
	- La classe Joueur : cette classe contient toute les infos necessaire sur le joueur/client, sa position, son score, son pseudo, son port UDP, la partie a laquel il est inscrit, et le nom de la machine du client pour savoir ou diffusé les message qu'un autre joueur pourrait lui envoyer.
