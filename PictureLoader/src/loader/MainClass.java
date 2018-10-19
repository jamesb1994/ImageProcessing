package loader;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import loader.view.PictureOverviewController;

public class MainClass extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Picture Analyser");
		
		initRootLayout();
		showPictureOverview();
		
		//this.primaryStage.getIcons().add(new Image ("file:Resources/img.jpg"));
		
		this.primaryStage.getIcons().add(new Image(ClassLoader.getSystemResourceAsStream("resources/img.jpg")));
		
		
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainClass.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			Scene scene = new Scene (rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void showPictureOverview() {
		//Load the picutre overview fxml file
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainClass.class.getResource("view/PictureOverview.fxml"));
		try {
			AnchorPane pictureOverview = (AnchorPane) loader.load();
			//Put picture overview in the middle of the root layout
			rootLayout.setCenter(pictureOverview);
			//Give picture controller access to the main app, for the file chooser in the controller
			PictureOverviewController pictureController = loader.getController();
			pictureController.setMain(this);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}
}
