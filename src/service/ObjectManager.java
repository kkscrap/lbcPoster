package service;


import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import fr.doodle.dao.CommuneDao;
import fr.doodle.dao.CompteLbcDao;
import scraper.Add;
import scraper.AddsGenerator;
import scraper.AgentLbc;
import scraper.Commune;
import scraper.CompteLbc;
import scraper.CritereSelectionTitre;
import scraper.CriteresSelectionVille;
import scraper.PathToAdds;
import scraper.Source;
import scraper.Texte;
import scraper.Title;

public class ObjectManager {

	private int nbAddsToPublish;

	private AgentLbc agentLbc;
	private AddsGenerator addsGenerator;
	private AddsSaver addsSaver;

	private List<CompteLbc> comptes;
	private CompteLbc compteInUse;

	private Source titleSourceType;
	private Source texteSourceType;
	private Source communeSourceType;

	private List<Title> titleSource;
	private List<Texte> texteSource;
	private List<Commune> communeSource;

	private CritereSelectionTitre critSelectTitre;
	private CriteresSelectionVille critSelectVille;

	private PathToAdds pathToAdds; 

	private List<Add> addsReadyToSave;
	ListIterator<Add> itOnAddReadyTosave;

	// DAO
	private CommuneDao comDao = new CommuneDao();;

	public void saveCodePostalAndNomCommuneNoCorrep(String idCommuneCorrespo) {
		int idCommuneCorresp = Integer.parseInt(idCommuneCorrespo);
		Commune commune = itOnAddReadyTosave.previous().getCommune();
		Commune communeCorresp = comDao.findOne(idCommuneCorresp);
		/* on met � jour le code postal */
		communeCorresp.setCodePostal(commune.getCodePostal());
		comDao.updateCodePostal(communeCorresp);
		/* on met � jour le nom */
		communeCorresp.setNomCommuneInBase(commune.getNomCommuneOnLbc());
		comDao.updateNomCommune(communeCorresp);
		itOnAddReadyTosave.next();
	}

	public void saveCodePostal(){
		Commune commune = itOnAddReadyTosave.previous().getCommune();
		if(commune.getCodePostal() ==null){
			System.out.println("dedans");
			comDao.updateCodePostal(commune);
		}
		itOnAddReadyTosave.next();
	}

	public List<Commune> search(String nameCommuneInBdd){
		List<Commune> communes = comDao.findAll(nameCommuneInBdd);
		return communes;
	}

	public void lancerControlCompte() {
		// TODO Auto-generated method stub

		agentLbc.setUp();
		agentLbc.connect();
		this.addsReadyToSave = agentLbc.controlCompte(); // pour r�cup�rer les annonces control�s
		addsSaver = new AddsSaver(addsReadyToSave); // pour faire le liene entres les annonces Lbc et la bdd (mettre � jour les ref)
		addsSaver.prepareAddsToSaving();
		itOnAddReadyTosave = addsReadyToSave.listIterator();
	}

	// pour it�rer sur les communes des adds pr�tes � �tre sauvegard�es
	public Title nextTitleReadyTosave(){
		return itOnAddReadyTosave.next().getTitle();
	}
	public Texte nextTexteReadyTosave() {
		return itOnAddReadyTosave.next().getTexte();
	}
	public Texte previousTexteReadyTosave() {
		return itOnAddReadyTosave.previous().getTexte();
	}
	
	public Title previousTitleReadyTosave() {
		return itOnAddReadyTosave.previous().getTitle();
	}
	
	public Commune nextCommuneReadyTosave(){
		return itOnAddReadyTosave.next().getCommune();
	}

	public boolean hasNextAddReadyTosave(){	
		boolean retour = itOnAddReadyTosave.hasNext();
		if(!retour){
			itOnAddReadyTosave = addsReadyToSave.listIterator();
		}
		return retour;
	}

	public void lancerPublication() {
		// g�n�ration des annonces
		addsGenerator.setImage();
		addsGenerator.generateAdds();
		agentLbc.setAddsToPublish(addsGenerator.getaddsProduced());
		agentLbc.setUp();
		agentLbc.connect();
		agentLbc.goToFormDepot();
		agentLbc.publish();
	}

	public void setcommunes() {
		addsGenerator.setCommuneSource();
		communeSource = addsGenerator.getCommuneSource();
	}

	public CriteresSelectionVille getCritSelectVille() {
		return critSelectVille;
	}

	public void setCritSelectVille(int borneInf, int bornSup) {
		this.critSelectVille = new CriteresSelectionVille();
		critSelectVille.setBornInfPop(borneInf);
		critSelectVille.setBornSupPop(bornSup);
		addsGenerator.setCritSelectVille(this.critSelectVille);
	}


	public void createAddsGenerator(){
		
		addsGenerator = new AddsGenerator(nbAddsToPublish);
		addsGenerator.saveTexteXlsxInBdd();
	}

	public void setTextes() {
		addsGenerator.setTexteSource();
		texteSource = addsGenerator.getTexteSource();
	}

	public void setTitres(){
		addsGenerator.setTitleSource();
		titleSource = addsGenerator.getTitleSource();
	}

	// pour r�cup�rer tous les comptes
	public void setComptes(){
		CompteLbcDao compteDao = new CompteLbcDao();
		comptes = compteDao.findAll(); 
	}

	public void setCompte(int identifiant){
		for(CompteLbc compte : comptes){
			if(compte.getIdAdmin() == identifiant){
				compteInUse = compte;
				return;
			}
		}
	}




	public void setPathToAdds(String pathToAdds) {
		this.pathToAdds = PathToAdds.valueOf(pathToAdds);
		addsGenerator.setPathToAddsDirectory(this.pathToAdds.getPath());
	}

	public int getNbAddsToPublish() {
		return nbAddsToPublish;
	}

	public void setNbAddsToPublish(int nbAddsToPublish) {
		this.nbAddsToPublish = nbAddsToPublish;
	}

	public void createAgentLbc(int nbAddsToPublish){
		agentLbc = new AgentLbc(compteInUse, nbAddsToPublish);
		setNbAddsToPublish(nbAddsToPublish);
	}

	public void createAgentLbc(){
		agentLbc = new AgentLbc(compteInUse);
	}

	public CompteLbc getCompteInUse() {
		return compteInUse;
	}

	public List<CompteLbc> getComptes() {
		return comptes;
	}

	public Source getTitleSourceType() {
		return titleSourceType;
	}

	public void setTitleSourceType(String titleSourceType) {
		this.titleSourceType = Source.valueOf(titleSourceType);
		addsGenerator.setTypeSourceTitles(this.titleSourceType);
	}

	public Source getTexteSourceType() {
		return texteSourceType;
	}

	public void setTexteSourceType(String texteSourceType) {
		this.texteSourceType = Source.valueOf(texteSourceType);
		addsGenerator.setTypeSourceTextes(this.texteSourceType);
	}

	public Source getCommuneSourceType() {
		return communeSourceType;
	}

	public void setCommuneSourceType(String communeSourceType) {
		this.communeSourceType = Source.valueOf(communeSourceType);
		addsGenerator.setTypeSourceCommunes(this.communeSourceType);
	}

	public AgentLbc getAgentLbc() {
		return agentLbc;
	}

	public void setAgentLbc(AgentLbc agentLbc) {
		this.agentLbc = agentLbc;
	}

	public AddsGenerator getAddsGenerator() {
		return addsGenerator;
	}

	public void setAddsGenerator(AddsGenerator addsGenerator) {
		this.addsGenerator = addsGenerator;
	}

	public List<Title> getTitleSource() {
		return titleSource;
	}

	public void setTitleSource(List<Title> titleSource) {
		this.titleSource = titleSource;
	}

	public List<Texte> getTexteSource() {
		return texteSource;
	}

	public void setTexteSource(List<Texte> texteSource) {
		this.texteSource = texteSource;
	}

	public List<Commune> getCommuneSource() {
		return communeSource;
	}

	public void setCommuneSource(List<Commune> communeSource) {
		this.communeSource = communeSource;
	}

	public CritereSelectionTitre getCritSelectTitre() {
		return critSelectTitre;
	}

	public void setCritSelectTitre(CritereSelectionTitre critSelectTitre) {
		this.critSelectTitre = critSelectTitre;
	}

	public PathToAdds getPathToAdds() {
		return pathToAdds;
	}

	public void setPathToAdds(PathToAdds pathToAdds) {
		this.pathToAdds = pathToAdds;
	}


	public void setComptes(List<CompteLbc> comptes) {
		this.comptes = comptes;
	}

	public void setCompteInUse(CompteLbc compteInUse) {
		this.compteInUse = compteInUse;
	}

	public void setTitleSourceType(Source titleSourceType) {
		this.titleSourceType = titleSourceType;
	}

	public void setTexteSourceType(Source texteSourceType) {
		this.texteSourceType = texteSourceType;
	}

	public void setCommuneSourceType(Source communeSourceType) {
		this.communeSourceType = communeSourceType;
	}


















}
