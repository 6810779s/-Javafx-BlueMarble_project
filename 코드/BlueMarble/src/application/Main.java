package application;

import Controller.AppController;
import Controller.LoginController;
import conn.PlayerClient;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	// define your offsets here
	private double xOffset = 0;
	private double yOffset = 0;
	PlayerClient SocketConnect;
	LoginController loginController;
	AppController appController;	
	FXMLLoader loader;
	Parent login;
	AnchorPane root;
	int closecnt = 0;
	

	@Override
	public void init() throws Exception {
		//SocketConnect = new PlayerClient("118.220.96.181"); // 소켓 접속
		SocketConnect = new PlayerClient("172.30.1.26");
		FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml")); //로그인화면 설정
		login = loginLoader.load();
		loginController = loginLoader.getController();

		FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/fxml/Root.fxml")); // 게임화면 설정
		root = rootLoader.load();
		appController = rootLoader.getController();
		appController.setSocket(SocketConnect); // app컨트롤러에 소켓접속정보를 보냄
		loginController.setSocket(SocketConnect);// login컨트롤러에 소켓 정보를 보냄
		loginController.setRoot(root); //login컨트롤러에 root정보를 보냄
		loginController.setAppController(appController); //login컨트롤러에 app컨트롤러 정보를 보냄
		super.init();
	}

	@Override
	
	public void start(Stage primaryStage) {
		try {
			primaryStage.initStyle(StageStyle.DECORATED);
			primaryStage.setMaximized(false);

			login.setOnMousePressed(new EventHandler<MouseEvent>() { //마우스 클릭 이벤트
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});

			login.setOnMouseDragged(new EventHandler<MouseEvent>() { //마우스 드래그 이벤트
				@Override
				public void handle(MouseEvent event) {
					primaryStage.setX(event.getScreenX() - xOffset);
					primaryStage.setY(event.getScreenY() - yOffset);
				}
			});
			Scene scene = new Scene(login);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		SocketConnect.getOutMsg().println("CheckMatching"); //매칭이 되었는지 확인
		super.stop();
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}