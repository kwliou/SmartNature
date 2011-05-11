package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

public class GardenGnome extends Application {
	private static ArrayList<Garden> gardens = new ArrayList<Garden>();
	private static DatabaseHelper dbHelper;
	
	public static ArrayList<Garden> getGardens() { return gardens; }
	
	public static Garden getGarden(int index) { return gardens.get(index); }
	
	public static int indexOf(Garden garden) { return gardens.indexOf(garden); }
	
	public static void init(Context context) {
		if (dbHelper != null)
			return;
		dbHelper = new DatabaseHelper(context);
		gardens.addAll(dbHelper.selectGardens());
		for (Garden garden : gardens) {
			loadPhotos(garden);
			loadPlots(garden);
		}
	}
	
	public static void loadPhotos(Garden garden) {
		ArrayList<Photo> photos = dbHelper.selectPhotos(garden);
		garden.getPhotos().addAll(photos);
	}
	
	public static void loadPlots(Garden garden) {
		ArrayList<Plot> plots = dbHelper.selectPlots(garden);
		for (Plot plot : plots)
			loadPlants(plot);
		
		garden.getPlots().addAll(plots);
	}
	
	public static void loadPlants(Plot plot) {
		ArrayList<Plant> plants = dbHelper.selectPlants(plot);
		for (Plant plant : plants)
			loadEntries(plant);
		
		plot.getPlants().addAll(plants);
	}
	
	public static void loadEntries(Plant plant) {
		ArrayList<Entry> entries = dbHelper.selectEntries(plant);
		plant.getEntries().addAll(entries);
	}
	
	/** adds entire garden info to database */
	public static void addServerGarden(Garden garden) {
		addGarden(garden);
		
		for (Photo photo : garden.getPhotos())
			dbHelper.insertPhoto(garden, photo);
		
		for (Plot plot : garden.getPlots()) {
			dbHelper.insertPlot(garden, plot);
			for (Plant plant : plot.getPlants()) {
				dbHelper.insertPlant(plot, plant);
				for (Entry entry : plant.getEntries())
					dbHelper.insertEntry(plant, entry);
			}
		}
	}
	
	public static void addGarden(Garden garden) {
		dbHelper.insertGarden(garden);
		gardens.add(garden);
		System.out.println("garden_id= " + garden.getId());
	}
	
	public static void addPhoto(Garden garden, Photo photo) {
		dbHelper.insertPhoto(garden, photo);
		garden.addPhoto(photo);
		System.out.println("photo_id= " + photo.getId());
	}
	
	public static void addPlot(Garden garden, Plot plot) {
		dbHelper.insertPlot(garden, plot);
		garden.addPlot(plot);
		System.out.println("plot_id= " + plot.getId());
	}
	
	public static void addPlant(Plot plot, Plant plant) {
		dbHelper.insertPlant(plot, plant);
		plot.addPlant(plant);
		System.out.println("plant_id= " + plant.getId());
	}
	
	public static void addEntry(Plant plant, Entry entry) {
		dbHelper.insertEntry(plant, entry);
		plant.addEntry(entry);
		System.out.println("entry_id= " + entry.getId());
	}
	
	public static void removeGarden(int index) { removeGarden(gardens.get(index)); }
	
	public static void removeGarden(Garden garden) {
		while (!garden.getPlots().isEmpty())
			removePlot(garden, garden.getPlot(0));
		for (Photo photo : garden.getPhotos())
			dbHelper.deletePhoto(photo);
		dbHelper.deleteGarden(garden);
		gardens.remove(garden);
	}
	
	public static void removePhoto(Garden garden, Photo photo) {
		dbHelper.deletePhoto(photo);
		garden.getPhotos().remove(photo);
	}
	
	public static void removePlot(Garden garden, Plot plot) {
		while (!plot.getPlants().isEmpty())
			removePlant(plot, plot.getPlant(0));
		dbHelper.deletePlot(plot);
		garden.getPlots().remove(plot);
	}
	
	public static void removePlant(Plot plot, Plant plant) {
		for (Entry entry : plant.getEntries()) 
			dbHelper.deleteEntry(entry);
		dbHelper.deletePlant(plant);
		plot.getPlants().remove(plant);
	}
	
	public static void removeEntry(Plant plant, Entry entry) {
		dbHelper.deleteEntry(entry);
		plant.getEntries().remove(entry);
	}
	
	public static void updateGarden(Garden garden) { dbHelper.updateGarden(garden); }
	
	public static void updatePhoto(Photo photo) { dbHelper.updatePhoto(photo); }
	
	public static void updatePlot(Plot plot) { dbHelper.updatePlot(plot); }
	
	public static void updatePlant(Plant plant) { dbHelper.updatePlant(plant); }
	
	public static void updateEntry(Entry entry) { dbHelper.updateEntry(entry); }
	
}
