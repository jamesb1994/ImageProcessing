package loader.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import loader.MainClass;
import loader.model.Algorithms;
import loader.model.Particle;
import loader.model.ParticleDatabase;

public class PictureOverviewController {

	@FXML
	private Button loadPictureButton;

	@FXML
	private Button copyPictureButton;

	@FXML
	private Button findEdgesButton;

	@FXML
	private Button filterEdgesStrengthButton;

	@FXML
	private TextField darkValueInput;

	@FXML
	private TextField thresholdValueInput;

	@FXML
	private TextField edgeMagnitudeValue;

	@FXML
	private ImageView imageViewMain;

	@FXML
	private ImageView imageViewBinaryThresh;

	@FXML
	private ImageView imageViewBgr;

	@FXML
	private ImageView imageViewDifference;

	@FXML
	private ImageView imageViewWithOnlyEdges;

	@FXML
	private ImageView imageViewFilteredEdges;
	
	@FXML
	private TableView<Particle>particleTable;
	
	@FXML
	private TableColumn<Particle, Integer> particleCountColumn;
	
	@FXML
	private TableColumn<Particle, Double>particleAreaColumn;
	
	@FXML
	private TableColumn<Particle, Double>particleShapeFactorColumn;
	
	@FXML
	private TableColumn<Particle, Double>particlePerimeterColumn;
	
	@FXML
	private TableColumn<Particle, Double>particleSizeColumn;
	
	@FXML 
	private TableColumn<ParticleDatabase, Integer>particleNumberColumn;
	
	@FXML
	private TableColumn<Particle, Integer> indexColumn;
	
	@FXML
	private Label heightLabel;

	@FXML
	private Label widthLabel;

	@FXML
	private Label pixelCountLabel;

	@FXML
	private Label darkPixeCountLabel;
	

	private MainClass main;
	private BufferedImage bufferedImage;
	private BufferedImage bufferedImageBackground;
	private BufferedImage imageAfterThresh;
	private BufferedImage binaryImageAfterThresh;
	private boolean isLoaded = false;
	private boolean isBgrLoaded = false;
	private boolean isThresh = false;
	private Algorithms algors;
	private ParticleDatabase particleDatabase;
	
	private ObservableList<Particle>particleObservableList;

	public PictureOverviewController() {
		this.algors = new Algorithms();
		this.particleObservableList = FXCollections.observableArrayList();

	}

	@FXML
	private void initialize() {
		
		
		


	}

	public void setMain(MainClass main) {
		this.main = main;
	}

	@FXML
	private void handleLoadPicture() {

		FileChooser selectImage = new FileChooser();
		selectImage.setTitle("Select Image File");
		selectImage.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP File .bmp", "*.bmp"),
				new FileChooser.ExtensionFilter("JPEG Files .jpg", "*.jpg"),
				new FileChooser.ExtensionFilter("PNG Files .png", "*.png"));
		selectImage.setInitialDirectory(new File("C:\\Users\\JamesBlake\\Desktop"));
		File file = selectImage.showOpenDialog(main.getPrimaryStage());
		if (file == null) {
			System.out.println("No file");
		} else {
			try {
				bufferedImage = ImageIO.read(file);
				Image image = SwingFXUtils.toFXImage(bufferedImage, null);
				imageViewMain.setImage(image);
				isLoaded = true;
				BufferedImage originalImage = SwingFXUtils.fromFXImage(image, null);
				this.algors.setOriginalImage(originalImage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	@FXML
	public void handleLoadBackground() {
		FileChooser selectBgrImage = new FileChooser();
		selectBgrImage.setTitle("Select Background Image File");
		selectBgrImage.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP File .bmp", "*.bmp"),
				new FileChooser.ExtensionFilter("PNG Files .png", "*.png"),
				new FileChooser.ExtensionFilter("JPEG Files .jpg", "*.jpg"));
		selectBgrImage.setInitialDirectory(new File("C:\\Users\\JamesBlake\\Desktop"));
		File file = selectBgrImage.showOpenDialog(main.getPrimaryStage());
		if (file == null) {
			System.out.println("No file");
		} else {
			try {
				bufferedImageBackground = ImageIO.read(file);
				Image imageBackground = SwingFXUtils.toFXImage(bufferedImageBackground, null);
				imageViewBgr.setImage(imageBackground);
				isBgrLoaded = true;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	public void handleProcessImageDifference() {

		if (isLoaded && !thresholdValueInput.getText().equals("") && isBgrLoaded) {
			if (!validThreshold(thresholdValueInput.getText())) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(main.getPrimaryStage());
				alert.setTitle("Thresholding Error");
				alert.setHeaderText("Cannot perform threshold");
				alert.setContentText("Please enter a threshold value between 0 and 255");

				alert.showAndWait();
				return;

			} else {
				this.algors.setThresholdValue(Integer.parseInt(thresholdValueInput.getText()));
				WritableImage imageAfterDifference = this.algors.backgroundDifferences(bufferedImage,
						bufferedImageBackground);
				imageAfterThresh = SwingFXUtils.fromFXImage(imageAfterDifference, null);

				imageViewDifference.setImage(imageAfterDifference);
			}

		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(main.getPrimaryStage());
			alert.setTitle("Thresholding Error");
			alert.setHeaderText("Cannot perform threshold");
			alert.setContentText("Please confirm image and background are loaded, and that you have "
					+ "input a threshold value");

			alert.showAndWait();
			return;
		}

		WritableImage binaryImageAfterDifference = this.algors.binaryImageAfterThresh(bufferedImage,
				bufferedImageBackground);
		binaryImageAfterThresh = SwingFXUtils.fromFXImage(binaryImageAfterDifference, null);
		imageViewBinaryThresh.setImage(binaryImageAfterDifference);
		isThresh = true;

		try {
			File file = new File("image.png");
			ImageIO.write(binaryImageAfterThresh, "png", file);
			System.out.println("done thresholding");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	public void handleFindEdges() {

		if (isThresh && !edgeMagnitudeValue.getText().equals("") && isBgrLoaded) {
			if (validEdge(edgeMagnitudeValue.getText())) {
				this.algors.setEdgeMagnitude(Integer.parseInt(edgeMagnitudeValue.getText()));

				WritableImage imageAfterEdges = this.algors.imageWithEdges(bufferedImage);
				BufferedImage imageWithEdges = SwingFXUtils.fromFXImage(imageAfterEdges, null);

				imageViewWithOnlyEdges.setImage(imageAfterEdges);

				File file2 = new File("imageEdges.png");
				try {
					System.out.println("Done processing edges");
					ImageIO.write(imageWithEdges, "png", file2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				setParticleDatabase();
				this.particleObservableList = FXCollections.observableArrayList
						(this.particleDatabase.getValidParticleList());
				particleTable.setItems(particleObservableList);
				
				particleCountColumn.setCellValueFactory(cellData -> 
					cellData.getValue().getPixelCountProperty().asObject());
				
				particleAreaColumn.setCellValueFactory(cellData -> cellData.getValue().getAreaProperty().asObject());
				particleShapeFactorColumn.setCellValueFactory(cellData -> 
					cellData.getValue().getShapeFactorProperty().asObject());
				particlePerimeterColumn.setCellValueFactory(cellData -> 
					cellData.getValue().getPerimeterProperty().asObject());
				particleSizeColumn.setCellValueFactory(cellData -> 
				cellData.getValue().getSizeProperty().asObject());
				indexColumn.setCellValueFactory(cellData -> cellData.getValue().getParticleIndex().asObject());
				
				
				
			} else {
				
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(main.getPrimaryStage());
				alert.setTitle("Edge Error");
				alert.setHeaderText("Cannot perform edge find");
				alert.setContentText("Please enter an appropriate edge strength");

				alert.showAndWait();
				return;
				
			}
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(main.getPrimaryStage());
			alert.setTitle("Edge Finding Ereor");
			alert.setHeaderText("Cannot perform edge find");
			alert.setContentText("Please confirm image has been thresholded, and that you have "
					+ "input an edge strength value");

			alert.showAndWait();
			return;
		}
		
		
		

	}


	public boolean validThreshold(String str) {
		if (str == null) {
			return false;
		}
		if (str.isEmpty()) {
			return false;
		}

		if (!str.matches("[0-9]+")) {
			return false;
		}

		if (Integer.parseInt(str) < 0 || Integer.parseInt(str) > 255) {
			return false;
		}

		return true;
	}

	public boolean validEdge(String str) {
		if (str == null) {
			return false;
		}
		if (str.isEmpty()) {
			return false;
		}

		if (!str.matches("[0-9]+")) {
			return false;
		}

		if (Integer.parseInt(str) < 0 || Integer.parseInt(str) > 500) {
			return false;
		}

		return true;
	}
	
	public void setParticleDatabase() {
		this.particleDatabase = this.algors.getDatabase();
		
	}
	

}
