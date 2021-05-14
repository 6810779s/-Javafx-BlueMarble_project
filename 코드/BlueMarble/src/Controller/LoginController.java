package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import conn.PlayerClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author oXCToo
 */
public class LoginController implements Initializable {
	@FXML
	private Label lblErrors,btnForgot,pw;

	@FXML
	private TextField txtUsername,txtPassword;

	@FXML
	private Button btnSignin,btnSignup;

	private Stage primaryStage;
	Connection con = null;
	PreparedStatement psmt = null;
	ResultSet rs = null;
	PlayerClient SocketConnect;
	String id = "success";
	int win;
	int lose;
	AnchorPane root;
	AppController appController;
	static String findPw;	// 찾은 패스워드

	public String getId() {
		return id;
	}

	public void setRoot(AnchorPane root) {
		this.root = root;
	}

	public void setAppController(AppController appController) {
		this.appController = appController;
	}

	@FXML
	public void handleButtonAction(MouseEvent event) throws SQLException { //버튼 이벤트

		if (event.getSource() == btnSignin) { //로그인			
			logIn();
			// login here
		} else if (event.getSource() == btnSignup) { //회원가입
			signup();
		}
	}
	
	@FXML
	public void handleLabelAction(MouseEvent event) throws SQLException { //라벨 이벤트
		if (event.getSource() == btnForgot) {
			forgotPW();
		}
	}
	
	public void forgotPW() { //비밀번호 찾기
		AnchorPane anchorPane = null;
		Stage signup = new Stage(StageStyle.UTILITY);
		signup.initModality(Modality.WINDOW_MODAL);
		signup.initOwner(primaryStage);
		signup.setTitle("Find Password");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextField name_signup = (TextField) anchorPane.lookup("#name_signup");// 이름 입력 필드 		
		TextField id_signup = (TextField) anchorPane.lookup("#id_signup");// 아이디 입력 필드
		TextField pw_signup = (TextField) anchorPane.lookup("#pw_signup");// 패스워드 입력필드
		Button signup_btn = (Button) anchorPane.lookup("#signup_btn"); //찾기 버튼 
		signup_btn.setText("Find");//찾기버튼 텍스트 설정
		Button back_btn = (Button) anchorPane.lookup("#back_btn");//뒤로가기 버튼
		Label lblErrors = (Label) anchorPane.lookup("#lblErrors");// 에러표시 라벨
		Label title = (Label) anchorPane.lookup("#title"); // 타이틀 
		Label pw = (Label) anchorPane.lookup("#pw"); //비밀번호
		title.setText("Find PW"); //타이틀 텍스트 설정
		pw.setVisible(false);
		pw_signup.setVisible(false);

		signup_btn.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {

					String name = name_signup.getText(); //입력된 이름 가져옴
					String id = id_signup.getText(); // 입력된 아이디 가져옴
					String sql = "SELECT pwd FROM MEMBER Where id = '" + id + "' and name = '" + name + "'";					
					if (name.isEmpty() || id.isEmpty()) { //빈칸이 있을때
						lblErrors.setTextFill(Color.RED);
						lblErrors.setText("Please, fill in all the blanks");//에러표시
					} else {//빈칸 없을때
						
						SocketConnect.getOutMsg().println("FindPW/" + sql); //서버에 쿼리 요청
						signup.close();
					}

				});
			}
		});

		back_btn.setOnAction(event -> signup.close());
		Scene scene = new Scene(anchorPane);
		signup.setScene(scene);
		signup.show();
	}
	
	public void signup() {  //회원가입
		AnchorPane anchorPane = null;
		Stage signup = new Stage(StageStyle.UTILITY);
		signup.initModality(Modality.WINDOW_MODAL);
		signup.initOwner(primaryStage);
		signup.setTitle("sign up");

		try {
			anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));

		} catch (IOException e) {			
			e.printStackTrace();
		}

		TextField name_signup = (TextField) anchorPane.lookup("#name_signup"); // 이름 입력 필드
		TextField id_signup = (TextField) anchorPane.lookup("#id_signup"); //아이디 입력 필드
		TextField pw_signup = (TextField) anchorPane.lookup("#pw_signup"); //패스워드 입력 필드
		Button signup_btn = (Button) anchorPane.lookup("#signup_btn"); //회원가입 완료 버튼
		Button back_btn = (Button) anchorPane.lookup("#back_btn"); //뒤로가기 버튼
		Label lblErrors = (Label) anchorPane.lookup("#lblErrors"); //에러 표시 라벨
		
		//회원가입 완료 버튼 클릭 이벤트
		signup_btn.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Platform.runLater(() -> {

					String name = name_signup.getText(); //입력된 이름을 가져옴
					String id = id_signup.getText(); //입력된 아이디를 가져옴
					String pw = pw_signup.getText(); // 입력된 비밀번호를 가져옴					
					
					String sql="select count(id) as cnt from member where id ='"+id+"'";
					if (name.isEmpty() || id.isEmpty() || pw.isEmpty()) { //빈칸이 있는지 체크
						lblErrors.setTextFill(Color.RED);
						lblErrors.setText("Please, fill in all the blanks");
					} else { //빈칸 없이 입력이 되었을또 동작
						//쿼리를 서버에 보내 DB에 요청
						SocketConnect.getOutMsg().println("Signup/" + sql+"/"+id+"/"+pw+"/"+name);
						signup.close();
					}

				});
			}
		});

		back_btn.setOnAction(event -> signup.close());
		Scene scene = new Scene(anchorPane);
		signup.setScene(scene);
		signup.show();
	}

	public void getMsg() { //서버 메시지를 받는 함수
		Thread thread = new Thread() {
			@Override
			public void run() {
				String[] rmsg = null;
				String msg = null;
				while (true) {
					try {
						msg = SocketConnect.getInMsg().readLine();
						System.out.println(msg);
					} catch (IOException e1) {
						System.out.println("소켓 통신 error");
					}
					if (msg != null) {						
						rmsg = msg.split("/");
						try {
							id = rmsg[0];
						} catch (Exception e2) {
							e2.getMessage();
						}
						if (msg.equals("Error")) {
							Platform.runLater(() -> {
								setLblError(Color.TOMATO, "ID/Password 가 틀렸습니다.");
								id = "Error";
								SocketConnect.setMsg(null);
							});
						} 
						
						// 패스워드 찾기
						else if(id.equals("FindPW")) {
							System.out.println("pw>>>>>"+rmsg[1]);
							findPw = rmsg[1];
							Platform.runLater(() ->{
								Stage dialog = new Stage(StageStyle.UTILITY);
								dialog.initModality(Modality.WINDOW_MODAL);
								dialog.initOwner(primaryStage);
								dialog.setTitle("패스워드 확인");
								AnchorPane anchorPane = null;

								try {
									anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/Message.fxml"));
								} catch (IOException e2) {
									e2.printStackTrace();
								}

								Label Message = (Label) anchorPane.lookup("#Message");
								if(findPw.equals("NotFound")) {
									Message.setText("존재하지 않는 사용자 입니다.");
								}else {
									String pw = "찾으시는 패스워드는 '"+ findPw +"' 입니다.";
									Message.setText(pw);
								}

								Button Complete = (Button) anchorPane.lookup("#Complete");
								Complete.setOnAction(event -> dialog.close());
								
								Scene scene = new Scene(anchorPane);
								dialog.setScene(scene);
								dialog.show();
							});
						}else if(id.equals("Signup")) { //회원가입 중복시 동작
							Platform.runLater(()->{
								setLblError(Color.TOMATO, "ID already exists.");
							});
						}else {
							Platform.runLater(() -> {
								setLblError(Color.GREEN, "Login Successful..Redirecting..");
							});
							String login = msg;
							Platform.runLater(() -> {
								Stage stage = (Stage) btnSignin.getScene().getWindow();								
								stage.close();
								gameRule(); //2021.4.24 수정
								appController.setPrimaryStage(stage);
								appController.setRoot(root);
								appController.setSocket(SocketConnect);
								appController.setLogin(login);
								appController.getMsg();
								Scene scene = new Scene(root);
								stage.setTitle("BlueMarble");
								stage.setScene(scene);
								stage.show();
								
							});
							break;
						}
					}
				}
			}
		};
		thread.start();
	}
	public void gameRule() {// 게임룰 표시
		Platform.runLater(() -> {
			AnchorPane anchorPane = null;

			Stage gameRule = new Stage(StageStyle.UTILITY);
			gameRule.initModality(Modality.WINDOW_MODAL);
			gameRule.initOwner(primaryStage);
			gameRule.setTitle("알림");

			try {
				anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/gameRule.fxml"));

			} catch (IOException e) {
				e.printStackTrace();
			}
		

			Scene scene = new Scene(anchorPane);
			gameRule.setScene(scene);
			gameRule.show();

		});
	}
	// 로그인 체크
	private void logIn() {
		String id = txtUsername.getText(); //입력된 아이디 가져옴
		String pwd = txtPassword.getText(); // 입력된 패스워드 가져옴
		System.out.println(id + "," + pwd);
		if (id.isEmpty() || pwd.isEmpty()) { //아이디와 패스워드 입력이 다 되어있는지 체크
			setLblError(Color.TOMATO, "Empty credentials"); //빈칸이 있을때
			id = "Error";
		} else {//빈칸이 없을때
			// query
			String sql = "SELECT id,WIN,LOSE FROM MEMBER Where id = '" + id + "' and pwd = '" + pwd + "'";
			SocketConnect.getOutMsg().println("Login/" + sql); //쿼리를 서버에 보내 DB에 요청

		}

	}

	private void setLblError(Color color, String text) { //라벨 설정
		lblErrors.setTextFill(color);
		lblErrors.setText(text);
		System.out.println(text);
	}

	public void setSocket(PlayerClient SocketConnect) { //소켓 설정
		this.SocketConnect = SocketConnect;
		if (SocketConnect == null) {
			lblErrors.setTextFill(Color.TOMATO);
			lblErrors.setText("Server Error : Check");
		} else {
			lblErrors.setTextFill(Color.GREEN);
			lblErrors.setText("Server is up : Good to go");
		}
		getMsg();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {		
	}
}