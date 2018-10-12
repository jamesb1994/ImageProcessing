package loader.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleDatabase {
	
	private List<Particle>particleDatabase;
	private List<Pixel>edgePixelDatabase;
	private List<Particle>validParticles;
	private List<Pixel>allPixelDatabase;
	private List<Double>particleArea;
	private Map<Integer, List<Particle>>particleStorageMap;
	private int currentCycle;
	
	public ParticleDatabase() {
		this.particleDatabase = new ArrayList<Particle>();
		this.edgePixelDatabase = new ArrayList<Pixel>();
		this.validParticles = new ArrayList<Particle>();
		this.allPixelDatabase = new ArrayList<Pixel>();
		this.particleArea = new ArrayList<Double>();
		this.particleStorageMap = new HashMap<Integer, List<Particle>>();
		this.currentCycle = 0;
	}
	
	public void add(Particle particle) {
		this.particleDatabase.add(particle);
	}
	
	
	
	public void addValid(Particle particle) {
		this.validParticles.add(particle);
		
		for (Pixel pixel : particle.getEdgePixels()) {
			this.edgePixelDatabase.add(pixel);
		}
		
		for (Pixel pixel : particle.getAllPixels()) {
			this.allPixelDatabase.add(pixel);
		}
		
	}
	
	public void addToStorage() {
		List<Particle>particleCycleList = new ArrayList<Particle>();
		
		for (Particle particle : this.validParticles) {
			particleCycleList.add(particle);
		}
		
		if (this.particleStorageMap.isEmpty()) {
			this.particleStorageMap.put(0, particleCycleList);
			this.currentCycle = 1;
		}else {
			this.particleStorageMap.put(currentCycle, particleCycleList);
			this.currentCycle++;
		}
	}
	
	public Map<Integer,List<Particle>>getStoredRuns(){
		return this.particleStorageMap;
	}
	
	public void printParticles() {
		for (Particle particle : this.particleDatabase) {
			System.out.println(particle);
		}
	}
	
	public void printEdgesParticles() {
		int partNo = 1;
		for (Particle particle : this.particleDatabase) {
			System.out.println(partNo);
			particle.printEdges();
			partNo++;
			System.out.println("");
		}
	}
	
	public List<Double>getAreaList(){
		for (Particle particle : this.validParticles) {
			this.particleArea.add(particle.getArea());
		}
		
		return this.particleArea;
	}
	
	public List<Particle> getParticleList(){
		return this.particleDatabase;
	}
	
	public List<Pixel>getAllEdgePixels(){
		return this.edgePixelDatabase;
	}
	
	public List<Pixel>getAllPixels(){
		return this.allPixelDatabase;
	}
	
	public List<Particle> getValidParticleList(){
		return this.validParticles;
	}
	
	public void clear() {
		this.particleDatabase.clear();
		this.edgePixelDatabase.clear();
		this.allPixelDatabase.clear();
	}
	
	

}
