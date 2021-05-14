package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import Data.PieceXY;
import Data.PlanetData;
import Data.PlayerData;
import conn.PlayerClient;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.VLineTo;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AppController implements Initializable {
   @FXML
   Button btn1, StartGame, btnGiveup;
   Button BuyPlanet, CostPlanet, choiceLand;
   @FXML
   ImageView C1, C2;
   @FXML
   StackPane Dice1, Dice2;
   @FXML
   ImageView DiceImg1, DiceImg2;
   @FXML
   TextField MyMoney, YourMoney;
   Label PlanetOwner;
   @FXML
   Label myName, yourName, myWinLose, yourWinLose, gameRule;
   @FXML
   Label card1, card2, Waiting, Double;
   @FXML
   Label cardcnt1, cardcnt2;
   Label curMoney;
   private static int TOP = 0;
   private static int LEFT = 1;
   private static int BOTTOM = 2;
   private static int RIGHT = 3;
   
   int d1; // 1번 주사위
   int d2; // 2번 주사위
   int Cardposi = 0; // 카드로 나온 포지션.
   int cardpass = 0;
   int cnt1 = 0;// 상대방 땅 개수
   int cnt2 = 0;// 내땅 개수
   int turn = 0;
   private Stage primaryStage;
   private AnchorPane root;
   ArrayList<PieceXY> pieceXY = new ArrayList<PieceXY>(); // 칸의위치
   ArrayList<PlanetData> planetData = new ArrayList<PlanetData>(); // 각 칸의 정보
   PathTransition pathTransition = new PathTransition();// 말의 애니매이션 -> 경로를 따라 움직음
   Path path;// 말의 경로 저장
   Popup passpopup = new Popup(); // 통행료 없이 지나가기 팝업
   ArrayList<Integer> secretCardList = new ArrayList<Integer>(); // 카드 이미지 번호 저장
   PlayerClient SocketConnect;
   int ready = 0;
   ArrayList<Stage> checkdialog = new ArrayList<Stage>();
   PlayerData myPlayer = new PlayerData();
   PlayerData yourPlayer = new PlayerData();

   @FXML
   public void handleLabelAction(MouseEvent event) throws SQLException { // 라벨 이벤트
      if (event.getSource() == gameRule) {
         gameRule();// 게임룰 화면 보여줌
      }
   }

   public void gameRule() { // 게임룰 화면 셋팅
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

   // 어플리케이션 시작시 초기화하는 함수
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      setSecretCard();
      Double.setVisible(false);
      Waiting.setVisible(false);
      MyMoney.setText("보유금액 : " + myPlayer.getMoney() + "원");
      MyMoney.setEditable(false);
      YourMoney.setText("보유금액 : " + yourPlayer.getMoney() + "원");
      YourMoney.setEditable(false);
      btn1.setDisable(true);
      btnGiveup.setDisable(true);
      btn1.setOnAction((event) -> rollTheDice());
      StartGame.setOnAction((event) -> StartGame());
      btnGiveup.setOnAction((event) -> {
         SocketConnect.getOutMsg().println("GameResult/" + yourPlayer.getId() + "/" + myPlayer.getId());
         gameFinish(yourPlayer.getId());
      });
   }

   // setLogin -> 로그인시 동작 함수
   public void setLogin(String login) {
      String[] rmsg = null;
      String msg = login;
      rmsg = msg.split("/");
      myPlayer.setId(rmsg[0]);
      myPlayer.setWin(Integer.parseInt(rmsg[1]));
      myPlayer.setLose(Integer.parseInt(rmsg[2]));
      Platform.runLater(() -> {
         myName.setText(myPlayer.getId());
         myWinLose.setText("승 : " + myPlayer.getWin() + " 패 : " + myPlayer.getLose());
      });

   }

   // StartGame ->버튼 눌렀을때 동작함수
   public void StartGame() {
      Thread thread = new Thread() {
         @Override
         public void run() {
            int cnt = 0;
            String text[] = { "Player Waiting", "Player Waiting.", "Player Waiting..", "Player Waiting...",
                  "Player Waiting....", "Player Waiting...." };
            // 서버에 게임준비 완료 메시지를 보냄
            SocketConnect.getOutMsg()
                  .println("Ready/" + myPlayer.getId() + "/" + myPlayer.getWin() + "/" + myPlayer.getLose());
            Platform.runLater(() -> {
               StartGame.setVisible(false);
               btnGiveup.setVisible(false);
               Waiting.setVisible(true);
            });

            while (true) { // 서버가 매칭을 잡아 줄때 까지 동작
               if (ready == 1) { // 서버에서 ready 값을 받아와 1 이되면 종료
                  Platform.runLater(() -> {
                     Waiting.setText("Game Start!"); // 게임 시작 메시지 표시
                     btnGiveup.setVisible(true);
                  });
                  try {
                     Thread.sleep(1000); // 1초동안 보여줌
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
                  Platform.runLater(() -> {
                     Waiting.setVisible(false);
                  });

                  break;
               }
               int t = cnt++;
               Platform.runLater(() -> {
                  Waiting.setText(text[t]); // 매칭대기 메시지 표시
               });

               if (cnt == 6) {
                  cnt = 0;
               }
               try {
                  Thread.sleep(100);
               } catch (InterruptedException e) {
                  e.printStackTrace();
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.start();

   }

   // gameFinish ->게임 종료시 동작함수
   public void gameFinish(String winner) {
      Stage dialog = new Stage(StageStyle.UTILITY);
      dialog.initModality(Modality.WINDOW_MODAL);
      AnchorPane anchorPane = null;
      try {
         anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/fin_dialog.fxml"));
      } catch (IOException e2) {
         e2.printStackTrace();
      }
      Label FinMessage = (Label) anchorPane.lookup("#FinMessage");
      Button FinButton = (Button) anchorPane.lookup("#FinButton");
      FinMessage.setText(winner + " 승리");
      FinButton.setOnAction(e -> {
         dialog.close();
         SocketConnect.getOutMsg().println("GameFinish"); // 서버에 게임종료를 알려줌
         primaryStage.close();
      });
      Scene scene = new Scene(anchorPane);
      dialog.setScene(scene);
      dialog.setAlwaysOnTop(true);
      dialog.show();
      for (int i = 0; i < checkdialog.size(); i++) {
         checkdialog.get(i).close(); // 열려있는 다이얼 로그를 다 종료 시킴
      }
   }

   // getMsg -> 소켓 메세지를 받는 함수
   public void getMsg() {
      Thread thread = new Thread() {
         @Override
         public void run() {
            String[] rmsg = null;
            String msg = null;
            while (true) {
               try {
                  msg = SocketConnect.getInMsg().readLine(); // 서버에서 보낸 메시지를 읽음.
                  System.out.println(msg);
               } catch (IOException e1) {
                  System.out.println("소켓에러");
               }
               if (msg != null) {
                  rmsg = msg.split("/");

                  if (rmsg[0].equals("Start")) { // 메세지 태그가 Start 일때 동작
                     yourPlayer.setId(rmsg[1]); // 상대방 아이디 저장
                     yourPlayer.setWin(Integer.parseInt(rmsg[2])); // 상대방 승리 저장
                     yourPlayer.setLose(Integer.parseInt(rmsg[3]));// 상대방 패배 저장
                     myPlayer.setTurn(Integer.parseInt(rmsg[4])); // 나의 턴 저장
                     Platform.runLater(() -> {
                        yourName.setText(yourPlayer.getId());
                        yourWinLose.setText("승 : " + yourPlayer.getWin() + " 패 : " + yourPlayer.getLose());

                     });

                     yourPlayer.setTurn((myPlayer.getTurn() + 1) % 2);
                     // user[myPlayer.getTurn()] = id;
                     // user[yourPlayer.getTurn()] = tname;
                     // System.out.println("user[0] : " + user[0] + " user[1] : " + user[1]);
                     if (myPlayer.getTurn() == 0) {
                        btn1.setDisable(false);
                        btnGiveup.setDisable(false);
                     }
                     ready = 1;// 매칭 ready값 변경
                  }
                  // SocketConnect.getOutMsg().println("Dice/" "/" + d1 + "/" + d2);
                  else if (rmsg[0].equals("Dice")) { // 메세지 태그가 Dice일때 동작
                     d1 = Integer.parseInt(rmsg[1]); // 주사위 1번값
                     d2 = Integer.parseInt(rmsg[2]); // 주사위 2번값
                     System.out.println("내턴 아님...");
                     Platform.runLater(() -> {
                        setDiceImage(); // 받아온 주사위값으로 이미지 셋팅
                        movePiece(d1 + d2, yourPlayer); // 받아온 주사위1,2 값 만큼 말 이동
                     });

                     System.out.println("현재턴:" + turn);

                  }
                  // ChangeTurn/turn
                  else if (rmsg[0].equals("ChangeTurn")) { // 메세지 태그가 ChangeTurn 일때 동작
                     turn = Integer.parseInt(rmsg[1]); // 턴값을 저장
                     if (myPlayer.getTurn() != turn) { // 턴값에 따라 주사위버튼 활성화
                        Platform.runLater(() -> {
                           btn1.setDisable(true);
                           btnGiveup.setDisable(true);
                        });

                     } else {
                        Platform.runLater(() -> {
                           btn1.setDisable(false);
                           btnGiveup.setDisable(false);
                        });
                     }
                     

                  }
                  // SocketConnect.getOutMsg().println("MovePiece/"+ movedice + "/" +
                  // movecount);
                  else if (rmsg[0].equals("MovePiece")) { // 메세지 태그가 MovePiece 일때 동작
                     int movedice = Integer.parseInt(rmsg[1]); // 움직일 말 설정
                     int movecount = Integer.parseInt(rmsg[2]); // 움직이는 카운트 설정
                     PlayerData movePlayer;
                     if (movedice == myPlayer.getTurn()) {
                        movePlayer = myPlayer;
                     } else {
                        movePlayer = yourPlayer;
                     }
                     Platform.runLater(() -> {
                        movePiece(movecount, movePlayer); // 말 움직임
                     });

                  }
                  // 소켓
                  // SocketConnect.getOutMsg().println("SetFlag/"+position[turn]+"/"+turn);
                  else if (rmsg[0].equals("SetFlag")) {// 메세지 태그가 SetFlage 일때 동작
                     int tp = Integer.parseInt(rmsg[1]); // Flag를 설치할 위치 설정
                     int tt = Integer.parseInt(rmsg[2]); // Flag의 색깔 설정
                     PlayerData player;
                     if (tt == myPlayer.getTurn()) {
                        player = myPlayer;
                     } else {
                        player = yourPlayer;
                     }
                     Platform.runLater(() -> {
                        setFlag(tp, player); // Flag 설치
                     });

                  }
                  // 소켓
                  // SocketConnect.getOutMsg()
                  // .println("SetBuilding/" + position[turn] +usernum+"/"+
                  // locate);
                  else if (rmsg[0].equals("SetBuilding")) {// 메세지 태그가 SetBuilding 일때 동작
                     int position = Integer.parseInt(rmsg[1]); // 설치할 위치 설정
                     int curturn = Integer.parseInt(rmsg[2]); // 턴값에 따른 색깔 설정
                     int locate = Integer.parseInt(rmsg[3]); // 1,2,3 번째에 맞는 위치 설정
                     PlayerData player;
                     if (curturn == myPlayer.getTurn()) {
                        player = myPlayer;
                     } else {
                        player = yourPlayer;
                     }
                     Platform.runLater(() -> {
                        setBuilding(position, locate, player);// 빌딩 설치

                     });

                  }
                  // 수정필요
                  else if (rmsg[0].equals("Money")) {// 메세지 태그가 Money 일때 동작
                     yourPlayer.setMoney(Integer.parseInt(rmsg[1])); // 상대 머니 설정
                     myPlayer.setMoney(Integer.parseInt(rmsg[2])); // 나의 머니 설정
                     Platform.runLater(() -> {
                        MyMoney.setText("보유금액 : " + myPlayer.getMoney() + "원"); // 나의 머니 표시
                        YourMoney.setText("보유금액 : " + yourPlayer.getMoney() + "원");// 상대의 머니 표시
                     });
                  }
                  // 수정필요
                  else if (rmsg[0].equals("Card")) {// 메세지 태그가 Card 일때 동작
                     yourPlayer.setCard(Integer.parseInt(rmsg[1])); // 상대 카드 설정
                     myPlayer.setCard(Integer.parseInt(rmsg[2]));// 나의 카드 설정
                     Platform.runLater(() -> {
                        cardcnt1.setText(myPlayer.getCard() + ""); // 나의 카드 표시
                        cardcnt2.setText(yourPlayer.getCard() + "");// 상대 카드 표시
                     });

                  }
                  // SocketConnect.getOutMsg().println("removeFlag/" + i + 1);
                  else if (rmsg[0].equals("removeFlag")) {// 메세지 태그가 removeFlag 일때 동작
                     int i = Integer.parseInt(rmsg[1]); // 받아온 위치값
                     int count = Integer.parseInt(rmsg[2]); // 건물 설치 갯수

                     Platform.runLater(() -> {// 설치된 건물 삭제
                        for (int j = count; j < planetData.get(i).getBuilding().size(); j++) {
                           root.getChildren().remove(planetData.get(i).getBuilding().get(j));

                        }

                        if (count == 0) {
                           planetData.get(i).setOwner("X");
                           planetData.get(i).setCount(0);
                        } else if (count == 1) {
                           planetData.get(i).setCount(1);
                        }
                     });
                  }

                  // 소켓
                  // SocketConnect.getOutMsg().println("ChangeLandFlag/" + position[turn] + "/"
                  // +curturn);
                  // SocketConnect.getOutMsg().println("ChangeLandFlag/" + landPosition + "/" +
                  // userNum + "/" + changePosition+ "/" +yournum);
                  else if (rmsg[0].equals("ChangeLandFlag")) {// 메세지 태그가 ChangeLandFlag 일때 동작
                	 
                     int tp = Integer.parseInt(rmsg[1]); // 위치1
                     int tt = Integer.parseInt(rmsg[2]); // 턴 1
                     int yourtp = Integer.parseInt(rmsg[3]);// 위치2
                     int yourtt = Integer.parseInt(rmsg[4]);// 턴2
                     int x,y;
                    
                     Platform.runLater(() -> {
                        ChangeLandFlag(tp, tt, yourtp, yourtt); // 건물 교환
                     });

                  }
                  // 비밀카드 보여주기
                  else if (rmsg[0].equals("ShowSecretCard")) {
                     int tturn = Integer.parseInt(rmsg[1]);
                     int num = Integer.parseInt(rmsg[2]);
                     Platform.runLater(() -> {
                        showSecretCard(num, tturn);
                     });

                  }
                  // 상대방 쉬게만드는 경우
                  else if (rmsg[0].equals("NextTurnBre")) {// 메세지 태그가 NextTurnBre 일때 동작
                     myPlayer.setBre(Integer.parseInt(rmsg[1])); // 나의 쉼값 설정
                  }
                  // 종료
                  else if (rmsg[0].equals("GameResult")) { // 메세지 태그가 GameResult 일때 동작
                     String winner = rmsg[1]; // 승자 설정
                     Platform.runLater(() -> {
                        gameFinish(winner); // 게임종료 실행
                     });
                  }
                  msg = null; // 메세지 초기화, 오류방지
               }
            }
         }
      };

      thread.start();
   }

   // setMoney -> 상황별 Money를 셋팅 하는 삼수
   public void setMoney(int my, int your) {
      int myMoney = myPlayer.getMoney(); // 현재 나의 머니 가져옴
      int yourMoney = yourPlayer.getMoney(); // 현재 상대의 머니 가져옴
      myMoney += my; // 머니 변경
      yourMoney += your; // 머니 변경
      myPlayer.setMoney(myMoney); // 변경된 나의 머니 설정
      yourPlayer.setMoney(yourMoney);// 변경된 상대의 머니 설정
      try {
         curMoney.setText("보유금액 : " + myPlayer.getMoney() + "원"); // 표시
      } catch (Exception e) {
         e.getMessage();
      }
      Platform.runLater(() -> {
         MyMoney.setText("보유금액 : " + myPlayer.getMoney() + "원"); // 나의 머니 표시
         YourMoney.setText("보유금액 : " + yourPlayer.getMoney() + "원"); // 상대의 머니 표시
         // 게임 상대방에게 변경된 값을 서버를 통해 전달.
         SocketConnect.getOutMsg().println("Money/" + myPlayer.getMoney() + "/" + yourPlayer.getMoney());
      });

      if (myPlayer.getMoney() < 0) { // 나의 머니가 0보다 작으면 게임 종료
         gameFinish(yourPlayer.getId()); // 게임종료 동작
         // 상대방에게도 게임종료를 서버를 통해 전당.
         SocketConnect.getOutMsg().println("GameResult/" + yourPlayer.getId() + "/" + myPlayer.getId());

      }else if(yourPlayer.getMoney()<0){
    	  gameFinish(myPlayer.getId()); // 게임종료 동작
          // 상대방에게도 게임종료를 서버를 통해 전당.
          SocketConnect.getOutMsg().println("GameResult/" + myPlayer.getId() + "/" + yourPlayer.getId());
      }
   }

   // movePiece 말의 움직임이 있을 때 동작
   public void movePiece(int num, PlayerData player) {
      Thread thread = new Thread() {
         @Override
         public void run() {
            path = new Path(); // 새로운 패스 설정
            Platform.runLater(() -> {
               for (int i = 0; i < num; i++) {
                  setPosition(player); // 패스 경로 설정
               }
               pathTransition.setDuration(Duration.millis(150 + 50 * num));
               pathTransition.setPath(path);
               if (player.getTurn() == 0) { // 움직일 말 선택
                  pathTransition.setNode(C1);
               } else {
                  pathTransition.setNode(C2);
               }
               pathTransition.play(); // 말 움직임

            });            
         }

      };
      thread.setDaemon(true);
      thread.start();
   }

   // rollTheDice 주사위 굴리기 버튼 클릭시 시작하는 함수.
   public void rollTheDice() {
      Double.setVisible(false);
      Thread thread = new Thread() {
         @Override
         public void run() {
            Platform.runLater(() -> {
               btn1.setDisable(true);// 주사위 버튼 비활성화
               btnGiveup.setDisable(true); // 포기버튼 비활성화
            });
            Platform.runLater(() -> {
               setDice(); // 주사위 랜덤 설정
            });
            Platform.runLater(() -> {
               setDiceImage(); // 변경된 주사위값으로 이미지 설정
            });
            try {
               Thread.sleep(50);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            if (myPlayer.getBre() != 0) { // 쉼값이 있을때
               if (d1 != d2) { // 더블이 아닐때
                  Platform.runLater(() -> {
                     Double.setText(planetData.get(myPlayer.getPosition()).getName() + " 탈출 실패 (더블 일경우 탈출)");
                     Double.setVisible(true);
                  });
                  myPlayer.bre--; // 쉼카운트 하나 감소
                  turn = yourPlayer.getTurn(); // 상대턴으로 변경
                  SocketConnect.getOutMsg().println("ChangeTurn/" + turn); // 변경된 턴값 서버를 통해 상대에게 보냄
                  return;
               } else {// 더블일떄
                  myPlayer.bre = 0; // 쉼값 초기화
                  Platform.runLater(() -> {
                     Double.setText("탈출 성공 ! 더블!! 한번 더");
                     Double.setVisible(true);
                  });
               }
            } else {// 쉼값이 0일때
               if (d1 != d2) { // 더블이 아닐떄
                  turn = yourPlayer.getTurn(); // 턴값 변경
               } else { // 더블일 경우
                  Platform.runLater(() -> {
                     Double.setText("더블!! 한번 더");
                     Double.setVisible(true);
                  });
               }
            }
            SocketConnect.getOutMsg().println("Dice/" + d1 + "/" + d2); // 주쉬에 굴린값을 서버를통해 상대에게 보냄
            // path = new Path();
            Platform.runLater(() -> {
               System.out.println("d1 : " + d1 + " d2 : " + d2);
               movePiece(d1 + d2, myPlayer); // 말을 움직임
            });
            try {
               Thread.sleep(150 + 50 * (d1 + d2) + 100);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            Platform.runLater(() -> {
               if (myPlayer.getCard() > 0) { // 패스 카드가 있으면
                  // 나의땅이 아닐때
                  if (planetData.get(myPlayer.getPosition()).getOwner().equals(yourPlayer.getId())) {
                     pass();
                  } else { // 나의땅일때
                     showDialog();
                  }
               } else {// 패스 카드 없을때
                  showDialog();
               }
            });
         }

      };
      thread.setDaemon(true);
      thread.start();
   }

   // 주사위 무작위 숫자 랜덤 셋팅
   public void setDice() {
      Random random = new Random();
      d1 = random.nextInt(6) + 1;
      d2 = random.nextInt(6) + 1;
      //      테스트 위해 주사위수 임의 조작
//      if(turn == 0) {
//         d1 = 2;
//         d2 = 3;
//      }else {
//         d1 = 2;
//         d2 = 3;
//      }      
   }

   // 주사위 이미지 셋팅
   public void setDiceImage() {
      String[] str = { "dice1.png", "dice2.png", "dice3.png", "dice4.png", "dice5.png", "dice6.png" };
      Image img = new Image("/images/" + str[d1 - 1]);
      Image img2 = new Image("/images/" + str[d2 - 1]);
      DiceImg1.setImage(img);
      DiceImg2.setImage(img2);

   }

   // 다이얼로그 띄울때 사용하는 함수
   public void showDialog() {
      String name = planetData.get(myPlayer.getPosition()).getName();
      String owner = planetData.get(myPlayer.getPosition()).getOwner();
      String data = planetData.get(myPlayer.getPosition()).getData();
      int price = planetData.get(myPlayer.getPosition()).getPrice();

      int count = planetData.get(myPlayer.getPosition()).getCount();

      AnchorPane anchorPane = null;
      Stage dialog = new Stage(StageStyle.UNDECORATED);
      Stage dialog2 = new Stage(StageStyle.UNDECORATED);
      dialog.initModality(Modality.WINDOW_MODAL);
      dialog.initOwner(primaryStage);
      dialog.setTitle("확인");
      dialog2.initModality(Modality.WINDOW_MODAL);
      dialog2.initOwner(primaryStage);
      dialog2.setTitle("확인");

      if (!owner.equals(myPlayer.getId()) && !owner.equals("X") && price != 0) { // 내 땅이 아니면 이용료 지불

         try {
            anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/Message.fxml"));
         } catch (IOException e2) {
            e2.printStackTrace();
         }

         Label Message = (Label) anchorPane.lookup("#Message");
         curMoney = (Label) anchorPane.lookup("#curMoney");

         Message.setText(name + "의 통행료 " + (price + (price / 2) * (count - 1)) + "원을 지불을 하였습니다.");
         setMoney(-price + (price / 2) * (count - 1), price + (price / 2) * (count - 1));

         Button Complete = (Button) anchorPane.lookup("#Complete");
         Complete.setOnAction(event -> {
            dialog.close();
            checkdialog.remove(dialog);
         });

         Scene scene = new Scene(anchorPane);
         dialog.setScene(scene);
         dialog.setAlwaysOnTop(true);
         dialog.show();
         checkdialog.add(dialog);

      } // 이용료 지불 끝

      try {
         if (name.equals("비밀카드")) { // 현재 위치가 비밀카드일때
            int num = setSecretCardNum();
            // 소켓
            SocketConnect.getOutMsg().println("ShowSecretCard/" + myPlayer.getTurn() + "/" + num);
            showSecretCard(num, myPlayer.getTurn());

         }

         else {
            anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/planetData.fxml"));
            BuyPlanet = (Button) anchorPane.lookup("#BuyPlanet");
            BuyPlanet.setVisible(true);
            CostPlanet = (Button) anchorPane.lookup("#CostPlanet");
            CostPlanet.setVisible(true);
            Label PlanetName = (Label) anchorPane.lookup("#PlanetName");
            PlanetOwner = (Label) anchorPane.lookup("#PlanetOwner");
            Label PlanetData = (Label) anchorPane.lookup("#PlanetData");
            PlanetName.setText(name);
            PlanetOwner.setText(owner);
            PlanetData.setText(data);
            // 구입 금액 셋팅
            int tp = 0;
            if (owner.equals(myPlayer.getId())) {
               if (count == 0) {
                  tp = price;
                  CostPlanet.setText("구입 금액 : " + tp);
               } else {
                  tp = (price / 2);
                  CostPlanet.setText("구입 금액 : " + tp);
               }

            } else {
               if (count == 0) {
                  tp = price;
                  CostPlanet.setText("구입 금액 : " + tp);
               } else {
                  tp = price + (price / 2) * (count - 1);
                  CostPlanet.setText("구입 금액 : " + tp);
               }
            }
            // 위치에따른 구매버튼, 구입가격 버튼 비활성화
            if (price == 0 || (myPlayer.getPosition()) == 0 || (count == 4 && owner.equals(myPlayer.getId()))) {
               BuyPlanet.setVisible(false);
               CostPlanet.setVisible(false);
            }
            // 탕꾸매 버튼이벤트 동작
            BuyPlanet.setOnAction(event -> {
               BuyLand(name, owner, price);
            });
            Button Complete = (Button) anchorPane.lookup("#Complete");

            // 특정 위치 정보 셋팅
            if (name.equals("워프")) {
               setWarp(anchorPane, dialog2);// 워프함수 동작
               Complete.setVisible(false);
            } else if (name.equals("조난기지")) {
               setMoney(-500000, 0);
               myPlayer.bre = 1; // 쉼값
               turn = yourPlayer.getTurn();
            } else if (name.equals("블랙홀")) {
               myPlayer.bre = 2; // 쉼값
               turn = yourPlayer.getTurn();
            } else {
               Complete.setVisible(true);
            }
            Complete.setOnAction(event -> {
               dialog2.close();
              
               SocketConnect.getOutMsg().println("ChangeTurn/" + turn);// 다이얼로그 종료시 턴 변경요청
               
               checkdialog.remove(dialog2);

            });
            Scene scene = new Scene(anchorPane);
            dialog2.setScene(scene);
            dialog2.show();
            checkdialog.add(dialog2);
         }
      } catch (IOException e2) {
         e2.printStackTrace();
      }

   }

   // ----------------------비밀카드 관련 함수 시작----------------------------
   // 비밀카드 셋팅
   public void setSecretCard() {
      for (int i = 0; i < 30; i++) {
         secretCardList.add(i);
      }
   }

   public void showSecretCard(int num, int actionTurn) {
      System.out.println("num : " + num);
      int cardNum;
      if (secretCardList.size() == 0) { // 모든 비밀카드가 오픈되고 나면 비밀카드 다시 채워줌
         setSecretCard();
         cardNum = secretCardList.get(num);
         secretCardList.remove(num);
      } else {
         cardNum = secretCardList.get(num);
         secretCardList.remove(num);
      }
      // 테스트용
      // cardNum = 9;
      System.out.println("cardNum : " + cardNum);
      System.out.println("secretCardList size : " + secretCardList.size());
      AnchorPane anchorPane = null;
      Stage dialog = new Stage(StageStyle.UNDECORATED);
      try {
         anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/secretCard.fxml"));
      } catch (IOException e) {
         e.printStackTrace();
      }
      ImageView secretCard = (ImageView) anchorPane.lookup("#secretCard");
      String imgName = cardNum + ".png";
      Image img = new Image("/images/secretCard/" + imgName);
      secretCard.setImage(img);
      Button Complete = (Button) anchorPane.lookup("#Complete");
      // int tmp = cardNum;
      Complete.setOnAction(event -> {
         dialog.close();
         // 5 7 14 16 17 24 29
         if (myPlayer.getTurn() == actionTurn && cardNum != 17 && cardNum != 29 && cardNum != 3 && cardNum != 9
               && cardNum != 19 && cardNum != 20) {
            SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
         }
         checkdialog.remove(dialog);

      });
      dialog.setAlwaysOnTop(true);
      if (myPlayer.getTurn() == actionTurn) {
         actionSecretCard(cardNum);
      }
      Scene scene = new Scene(anchorPane);
      dialog.setScene(scene);
      dialog.setX(primaryStage.getX() + 260);
      dialog.setY(primaryStage.getY() + 59.5);
      dialog.show();
      checkdialog.add(dialog);
   }

   // 비말카드 번호 랜덤으로 셋팅
   public int setSecretCardNum() {
      Random random = new Random();
      return random.nextInt(secretCardList.size());
   }
   /*
    * 카드 번호에 따라 실행되는 동작 함수
    */

   public void actionSecretCard(int num) {
      switch (num) {
      case 0: // 소유한 본인땅의 모든 건물 삭제

         for (int i = 0; i < planetData.size(); i++) {
            if (myPlayer.getId().equals(planetData.get(i).getOwner())) {
               for (int j = 1; j < planetData.get(i).getBuilding().size(); j++) {
                  SocketConnect.getOutMsg().println("removeFlag/" + i + "/" + 1);
               }
            }
         }

         break;
      case 1: // 후원금 30만원 받음
         setMoney(300000, 0);
         break;
      case 2: // 상대방에게 돈 30만원 뺏어옴
         setMoney(300000, -300000);
         // 상대편 금액 -30만원 로직 추가해야함. socket으로 -30만원 전송
         break;
      case 3: // 상대땅 하나 가져오기
         takeLand(0);
         break;
      case 4:// money[myPlayer.getTurn()] -= 1000000;
         setMoney(-1000000, 0);
         break;
      case 5://조난기지로 이동
         myPlayer.setBre(1);
         Cardposi = 1;
         int posi2 = myPlayer.getPosition();
         turn = yourPlayer.getTurn();
         setPosition2(posi2, myPlayer);
         break;

      case 6:
         setMoney(-300000, 300000);
         // 상대편 금액 +30만원 로직 추가해야함. socket으로 +30만원 전송
         break;
      case 7: // 블랙홀로 이동시키기.
         myPlayer.setBre(2);
         Cardposi = 2;
         posi2 = myPlayer.getPosition();
         turn = yourPlayer.getTurn();
         setPosition2(posi2, myPlayer);
         break;
      case 8://외계인 전투
         setMoney(-500000, 0);
         break;
      case 10:// 고장난 우주선
         setMoney(-500000, 0);
         break;

      case 9:// 당신의 땅과 상대편의 땅을 바꿉니다.
         takeLand(1);
         break;
      case 11:// 가장 비싼 땅을 반액에 팔음. 건물이 지어진 경우 반액에 처분.
         int max = 0;
         int tmp = 0;         
         for (int i = 0; i < planetData.size(); i++) {
            if (myPlayer.getId().equals(planetData.get(i).getOwner())) {
               max = (max > planetData.get(i).getPrice()) ? max : planetData.get(i).getPrice();
            }

         }
         for (int i = 0; i < planetData.size(); i++) {
            if (myPlayer.getId().equals(planetData.get(i).getOwner())) {
               for (int j = 0; j < planetData.get(i).getBuilding().size(); j++) {
                  if (max == planetData.get(i).getPrice()) {
                     SocketConnect.getOutMsg().println("removeFlag/" + i + "/" + 0);
                     if (tmp == 0) {
                        int tmoney = ((max + ((max / 2) * (planetData.get(i).getBuilding().size() - 1))) / 2);
                        setMoney(+tmoney, 0);
                        tmp++;
                     }
                  }
               }
            }
         }
         break;
      case 12:
         // 모든 땅 반납
         for (int i = 0; i < planetData.size(); i++) {
            if (myPlayer.getId().equals(planetData.get(i).getOwner())) {
               for (int j = 0; j < planetData.get(i).getBuilding().size(); j++) {
                  SocketConnect.getOutMsg().println("removeFlag/" + i + "/" + 0);
               }
               planetData.get(i).setOwner("X");
               planetData.get(i).setCount(0);
            }
         }
         break;
      case 13:
         // 보유금액 50% 차감
         setMoney(-(myPlayer.getMoney() / 2), 0);
         break;

      case 14:
         // 지구로 돌아감. 수고비 받지 못함. 20만원 차감.
         Cardposi = 3;
         posi2 = myPlayer.getPosition();
         setPosition2(posi2, myPlayer);
         setMoney(-200000, 0);
         break;
      case 15: // 한턴 쉽니다.
         myPlayer.setBre(1);
         turn = yourPlayer.getTurn();
         break;
      case 16:
         // 지구로 돌아갑니다. 수고비를 받습니다.
         Cardposi = 3;
         posi2 = myPlayer.getPosition();
         setPosition2(posi2, myPlayer);
         break;
      case 17: // 워프로 이동.
         Cardposi = 4;
         posi2 = myPlayer.getPosition();
         setPosition2(posi2, myPlayer);

         break;
      case 18: // 상대편 금액 -100만원 로직 추가해야함. socket으로 -100만원 전송
         setMoney(1000000, -1000000);
         break;
      case 19: // 원하는 땅 자신의 땅으로 만들음.
         takeLand(2);
         break;
      case 20: // 건물 두개 지을 땅 선택 (자신의 땅이여야함. 땅이 없을시 무효.)
         takeLand(3);
         break;

      case 21:
         setMoney(100000, 0);
         break;
      case 22: // 상대방 조난 기지에 보내기
         Cardposi = 1;
         posi2 = yourPlayer.getPosition();
         setPosition2(posi2, yourPlayer);
         SocketConnect.getOutMsg().println("NextTurnBre/" + 1);
         setMoney(0, -500000);
         break;
      // 서버 소켓..
      case 23: // 통행료 없이 땅 지나가기
         myPlayer.setCard(myPlayer.getCard() + 1);
         Platform.runLater(() -> {
            cardcnt1.setText(myPlayer.getCard() + "");
            cardcnt2.setText(yourPlayer.getCard() + "");
         });
         SocketConnect.getOutMsg().println("Card/" + myPlayer.getCard() + "/" + yourPlayer.getCard());
         break;

      case 24: // 10만원 내고 비밀 카드 한 장 더 뽑기
         setMoney(-100000, 0);
         Platform.runLater(() -> {
            oneMore();
         });
         break;

      case 25: // 생일
         setMoney(200000, -200000);

         break;

      case 26: // 주사위 한번 더
         turn = myPlayer.getTurn();
         break;
      case 27://약탈
         setMoney(200000, 0);
         break;
      case 28:
         setMoney(-myPlayer.getMoney(), 0);
         break;

      case 29:
         // 5칸 더 앞으로 전진.
         Cardposi = 6;
         posi2 = myPlayer.getPosition();
         setPosition2(posi2, myPlayer);
         break;

      }
   }

   // 비밀카드 내용 보여주기

   // 비밀카드 통행료 없이 지나가기
   public void pass() { // 통행료 없이 지나가기 함수.
      AnchorPane anchorPane = null;
      Stage pass = new Stage(StageStyle.UNDECORATED);
      pass.initModality(Modality.WINDOW_MODAL);
      pass.initOwner(primaryStage);
      pass.setTitle("알림");

      try {
         anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/pass.fxml"));

      } catch (IOException e) {
         e.printStackTrace();
      }
      Button Y = (Button) anchorPane.lookup("#Y");
      Button N = (Button) anchorPane.lookup("#N");

      Y.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
            Platform.runLater(() -> {
               myPlayer.setCard(myPlayer.getCard() - 1);
               SocketConnect.getOutMsg().println("Card/" + myPlayer.getCard() + "/" + yourPlayer.getCard());
               cardcnt1.setText(myPlayer.getCard() + "");

               cardpass = 0;
               try {
                  passpopup.setAutoFix(true);
                  passpopup.setAutoHide(true);
                  passpopup.setHideOnEscape(true);

                  passpopup.getContent().add(FXMLLoader.load(getClass().getResource("/fxml/passpopup.fxml")));
                  passpopup.show(primaryStage);
               } catch (IOException e1) {
                  e1.printStackTrace();
               }
               cardpass = 2;
               pass.close();
               SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
               checkdialog.remove(pass);
            });
         }
      });
      N.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
            showDialog();
            pass.close();
            SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
         }
      });

      Scene scene = new Scene(anchorPane);
      pass.setScene(scene);
      pass.show();
      checkdialog.add(pass);
   }

   // 비밀카드 한번더
   public void oneMore() {
      AnchorPane anchorPane = null;

      Stage dialog3 = new Stage(StageStyle.UNDECORATED);
      dialog3.initModality(Modality.WINDOW_MODAL);
      dialog3.initOwner(primaryStage);
      dialog3.setTitle("알림");

      try {
         anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/OneMoreCard.fxml"));

      } catch (IOException e) {
         e.printStackTrace();
      }
      Button Yes = (Button) anchorPane.lookup("#Yes");

      Button No = (Button) anchorPane.lookup("#No");

      Yes.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
            Platform.runLater(() -> {
               showDialog();
               dialog3.close();
               checkdialog.remove(dialog3);
            });
         }
      });
      No.setOnAction(event -> dialog3.close());

      Scene scene = new Scene(anchorPane);
      dialog3.setScene(scene);
      dialog3.show();
      checkdialog.add(dialog3);
   }

   // 비밀카드 땅 관련 동작 함수
   public void takeLand(int index) {
      Stage dialog = new Stage(StageStyle.UNDECORATED);
      dialog.initModality(Modality.WINDOW_MODAL);
      dialog.initOwner(primaryStage);
      if (index == 0) {
         dialog.setTitle("상대땅 가져오기");
      } else if (index == 1) {
         dialog.setTitle("땅 교환");
      } else if (index == 1) {
         dialog.setTitle("원하는 땅 내땅으로");
      }
      AnchorPane takeLandPane = null;

      try {
         takeLandPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/landChange.fxml"));
      } catch (IOException e2) {
         e2.printStackTrace();
      }
      ChoiceBox yourLand = (ChoiceBox) takeLandPane.lookup("#yourLand");
      ChoiceBox myLand = (ChoiceBox) takeLandPane.lookup("#myLand");
      Label myLandLabel = (Label) takeLandPane.lookup("#myLandLabel");
      Label yourLandLabel = (Label) takeLandPane.lookup("#yourLandLabel");
      if (index != 1 && index != 3) {
         myLand.setVisible(false);
         myLandLabel.setVisible(false);

      }

      switch (index) {
      case 0:// 상대땅 하나 가져오기
         for (PlanetData pd : planetData) {
            if (!pd.getOwner().equals(myPlayer.getId()) && !pd.getOwner().equals("X")) {
               yourLand.getItems().add(pd.getName());
            }
         }
         break;
      case 1:// 당신의 땅과 상대편의 땅을 바꿉니다.

         for (PlanetData pd : planetData) {
            if (!pd.getOwner().equals(myPlayer.getId()) && !pd.getOwner().equals("X")) {
               yourLand.getItems().add(pd.getName());
               cnt1++;
            } else if (pd.getOwner().equals(myPlayer.getId())) {
               myLand.getItems().add(pd.getName());
               cnt2++;
            }
         }

         break;
      case 2:// 원하는 땅 자신의 땅으로 만들음.
         for (PlanetData pd : planetData) {
            if (!pd.getOwner().equals(myPlayer.getId()) && pd.getPrice() != 0) {
               yourLand.getItems().add(pd.getName());
            }
         }
         break;
      case 3:// 건물 두개 지을 땅 선택 (자신의 땅이여야함. 땅이 없을시 무효.)
         yourLand.setVisible(false);
         yourLandLabel.setVisible(false);
         for (PlanetData pd : planetData) {
            if (pd.getOwner().equals(myPlayer.getId())) {
               if (pd.getCount() <= 2) {
                  myLand.getItems().add(pd.getName());
                  cnt1++;
               }
            }
         }
         break;
      }

      Button Complete = (Button) takeLandPane.lookup("#Complete");
      Complete.setOnAction(event -> {
         dialog.close();
         SocketConnect.getOutMsg().println("ChangeTurn/" + turn);
      });
      choiceLand = (Button) takeLandPane.lookup("#choiceLand");
      if (index == 1) {
         if (cnt1 != 0 && cnt2 != 0) {
            choiceLand.setOnAction(event -> changeLand(yourLand, myLand));
            cnt1 = 0;
            cnt2 = 0;
         } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("알림");
            alert.setHeaderText("카드 무효화");
            alert.setContentText("내땅 혹은 상대방 땅이 없으니 이 카드는 무효화 됩니다.");
            alert.showAndWait().filter(response -> response == ButtonType.OK)
                  .ifPresent(response -> SocketConnect.getOutMsg().println("ChangeTurn/" + turn));

            cnt1 = 0;
            cnt2 = 0;
            return;
         }
      } else if (index == 3) {
         if (cnt1 != 0) {
            choiceLand.setOnAction(event -> getChoice(myLand, index));
            cnt1 = 0;
         } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("알림");
            alert.setHeaderText("카드 무효화");
            alert.setContentText("땅이 없으므로 이 카드는 무효화 됩니다.");
            alert.showAndWait().filter(response -> response == ButtonType.OK)
                  .ifPresent(response -> SocketConnect.getOutMsg().println("ChangeTurn/" + turn));
            cnt1 = 0;
            cnt2 = 0;
            return;
         }

      } else {
         choiceLand.setOnAction(event -> getChoice(yourLand, index));
      }

      Scene scene = new Scene(takeLandPane);
      dialog.setScene(scene);
      dialog.show();
   }

   // 비밀카드로 땅 교환시 동작 함수
   private void changeLand(ChoiceBox<String> choiceYourBox, ChoiceBox<String> choiceMyBox) {
      int position1 = 0;
      int position2 = 0;
      String[] name = { choiceYourBox.getValue().toString(), choiceMyBox.getValue().toString() };
      System.out.println("name[0]"+name[0]);
      // System.out.println(myPlayer.getTurn() + "," + nextTurn + ":" + name[0] + ","
      // + name[1]);
      if (name[0] == null || name[1] == null) {
         return;
      }

      for (int i = 0; i < planetData.size(); i++) {
         if (planetData.get(i).getName().equals(name[0])) { // 선택한 상대 땅일때 동작
            // 땅 가져올때 플래그들 변경 함수
            position1 = i;
         }
         else if (planetData.get(i).getName().equals(name[1])) {
            position2 = i;
         }
       
      }
     
      ChangeLandFlag(position1, myPlayer.getTurn(), position2, yourPlayer.getTurn());

      choiceLand.setVisible(false);

   }

   // 비밀카드로 땅 선택시 동작 함수
   private void getChoice(ChoiceBox<String> choiceBox, int index) {
      String name = choiceBox.getValue();

      if (name == null) {
         return;
      }

      for (int i = 0; i < planetData.size(); i++) {
         if (planetData.get(i).getName().equals(name)) {
            // 선택한 땅일때 동작
            if (index == 3) { // 건물 두개 지음
               if (planetData.get(i).getCount() == 1) {
                  setBuilding(i, -33, myPlayer);
                  setBuilding(i, -10, myPlayer);
               } else if (planetData.get(i).getCount() == 2) {
                  setBuilding(i, -10, myPlayer);
                  setBuilding(i, 13, myPlayer);

               }

            } else {
               // 플래그 추가 혹은 변경
               if (!planetData.get(i).getOwner().equals("X")) { // 상대땅 가져오기 일때만 기존 플레그 제거
                  ChangeLandFlag(i, myPlayer.getTurn(), -1, -1); // 땅 가져올대 플래그들 변경 함수
               } else {
                  setFlag(i, myPlayer); // 플래그 설치 함수

               }
               // 소유자 변경
            }

         }
      }

      choiceLand.setVisible(false);
   }

   // ----------------------건물 관련 함수 시작-----------------------------------
   // 플래그 설치 함수
   public void setFlag(int landPosition, PlayerData player) {
      ImageView flag = new ImageView();
      Image im = new Image("/images/flag" + (player.getTurn() + 1) + ".png");// 턴에 따른 플래그 색깔 설정
      flag.setLayoutX(pieceXY.get(landPosition - 1).getPieceX());
      flag.setLayoutY(pieceXY.get(landPosition - 1).getPieceY());
      flag.setFitHeight(30);
      flag.setFitWidth(20);
      flag.setImage(im);
      root.getChildren().add(flag); // 화면에 추가
      // 데이터 추가
      planetData.get(landPosition).getBuilding().add(flag);
      planetData.get(landPosition).count++;
      planetData.get(landPosition).setOwner(player.getId());
      
      if (myPlayer == player) {
         // 상대방에게 서버를 통해 변경된 값 보냄
         SocketConnect.getOutMsg().println("SetFlag/" + landPosition + "/" + player.getTurn());
      }

   }

   // 건물 설치 함수 setBuilding(현재 위치값, 건물 설치 조정값)
   public void setBuilding(int landPosition, int located, PlayerData player) {
      Rectangle rec[] = new Rectangle[2];
      rec[0] = new Rectangle(pieceXY.get(landPosition - 1).getPieceX() + located,
            pieceXY.get(landPosition - 1).getPieceY() - 30, 20, 20);
      rec[0].setFill(Color.DODGERBLUE);
      rec[1] = new Rectangle(pieceXY.get(landPosition - 1).getPieceX() + located,
            pieceXY.get(landPosition - 1).getPieceY() - 30, 20, 20);
      rec[1].setFill(Color.RED);
      root.getChildren().add(rec[player.getTurn()]);
      planetData.get(landPosition).getBuilding().add(rec[player.getTurn()]);
      planetData.get(landPosition).setOwner(player.getId());
      planetData.get(landPosition).count++;
      if (myPlayer == player) {
         // 소켓
         SocketConnect.getOutMsg().println("SetBuilding/" + landPosition + "/" + player.getTurn() + "/" + located);
      }
    
   }
   

   // 상대땅 가져올때 설치되어 있는 플래그들 변경 함수
   public void ChangeLandFlag(int landPosition, int x, int changePosition, int y) {
	   
      for (int i = 0; i < planetData.get(landPosition).getBuilding().size(); i++) {
         // 상대편 땅에 있는 깃발 과 빌딩 색깔 바꿈ChangeTurn.
         if (y == -1 || y != -1) {
            root.getChildren().remove(planetData.get(landPosition).getBuilding().get(i));
            if (x == 0) {
               System.out.println("3");
               if (i == 0) {
                  Image im = new Image("/images/flag" + (x + 1) + ".png");
                  ((ImageView) planetData.get(landPosition).getBuilding().get(i)).setImage(im);
               } else {
                  ((Rectangle) planetData.get(landPosition).getBuilding().get(i)).setFill(Color.DODGERBLUE);
               }
               planetData.get(landPosition).setOwner(myPlayer.getId());
            } else {
               if (i == 0) {
                  Image im = new Image("/images/flag" + (x + 1) + ".png");
                  ((ImageView) planetData.get(landPosition).getBuilding().get(i)).setImage(im);
               } else {
                  ((Rectangle) planetData.get(landPosition).getBuilding().get(i)).setFill(Color.RED);
               }
               planetData.get(landPosition).setOwner(myPlayer.getId());
               
            }
            root.getChildren().add((Node) planetData.get(landPosition).getBuilding().get(i));
         }
         System.out.println("dddChangeLandFlag/"+landPosition+"/"+x+"/"+changePosition+"/"+y);
         // youtnum = -1일 경우 상대편 땅을 내땅으로 바꿈.
         // yournum != -1일 경우 상대편 당과 내땅 체인지.
         if (y != -1) {
            root.getChildren().remove(planetData.get(changePosition).getBuilding().get(i));
            if (y == 0) {
               if (i == 0) {
                  Image im = new Image("/images/flag" + (y + 1) + ".png");
                  ((ImageView) planetData.get(changePosition).getBuilding().get(i)).setImage(im);
               } else {
                  ((Rectangle) planetData.get(changePosition).getBuilding().get(i)).setFill(Color.DODGERBLUE);
               }
               planetData.get(changePosition).setOwner(yourPlayer.getId());

            } else if (y == 1) {
               if (i == 0) {
                  Image im = new Image("/images/flag" + (y + 1) + ".png");
                  ((ImageView) planetData.get(changePosition).getBuilding().get(i)).setImage(im);
               } else {
                  ((Rectangle) planetData.get(changePosition).getBuilding().get(i)).setFill(Color.RED);
               }
            }
            planetData.get(changePosition).setOwner(yourPlayer.getId());
            root.getChildren().add((Node) planetData.get(changePosition).getBuilding().get(i));
         }
        

      }

   //   if (myPlayer == player1) {
         // 소켓, 상대방에게 변경된 데이터를 서버를 통해 전달.
     
      Platform.runLater(()->{
    	  System.out.println("ChangeLandFlag");
    	  System.out.println(landPosition);
    	  System.out.println(x);
    	  System.out.println(changePosition);
    	  System.out.println(y);
//    	  SocketConnect.getOutMsg().println("ChangeLandFlag/" + landPosition + "/" + x + "/"
//                  + changePosition + "/" + y);  
      });
      SocketConnect.getOutMsg().println("ChangeLandFlag/" + landPosition + "/" + x + "/"
              + changePosition + "/" + y);  

         
    //  }

   }

   // 땅구매시 사용되는 함수
   public void BuyLand(String name, String owner, int price) {

      Stage dialog = new Stage(StageStyle.UNDECORATED);
      dialog.initModality(Modality.WINDOW_MODAL);
      dialog.initOwner(primaryStage);
      dialog.setTitle("구매 확인");
      int cost = 0;
      // 코스트 설정
      if (planetData.get(myPlayer.getPosition()).getCount() == 0) {
         cost = price;
      } else {
         cost = price + (price / 2) * (planetData.get(myPlayer.getPosition()).getCount() - 1);
      }

      AnchorPane anchorPane = null;

      try {
         anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/Message.fxml"));
      } catch (IOException e2) {
         e2.printStackTrace();
      }

      Label Message = (Label) anchorPane.lookup("#Message");
      curMoney = (Label) anchorPane.lookup("#curMoney");

      Button Complete = (Button) anchorPane.lookup("#Complete");

      Complete.setOnAction(event -> {
         dialog.close();
         checkdialog.remove(dialog);
      });
//         Rectangle rec[] = new Rectangle[2];

//         ImageView flag = new ImageView();
      System.out.println("cost : " + cost);
      System.out.println("price : " + price);
      if (myPlayer.getMoney() < cost) { // 보유금액과 코스트 비교, 돈이 부족할때
         Message.setText("보유금액이 부족합니다.");
      } else {// 돈이 충분할때
         
  
         if (planetData.get(myPlayer.getPosition()).getOwner().equals("X")) {
            planetData.get(myPlayer.getPosition()).setOwner(myPlayer.getId());
            PlanetOwner.setText(myPlayer.getId());
         }
         if (!planetData.get(myPlayer.getPosition()).getOwner().equals(myPlayer.getId())) { // 내땅이 아닐경우

            ChangeLandFlag(myPlayer.getPosition(), myPlayer.getTurn(), -1, -1); // 땅 가져올대 플래그들 변경 함수
            System.out.println("2myPlayer"+myPlayer.getTurn()+",yourPlayer"+yourPlayer.getTurn());
//            try {
//                Thread.sleep(50);
//             } catch (InterruptedException e) {
//                e.printStackTrace();
//             }
            setMoney(-cost, cost);
            Platform.runLater(()->{
                PlanetOwner.setText(myPlayer.getId());   
                 Message.setText("구매 완료하였습니다.");
            });
           
         } else { // 빈땅 이거나가 내땅인경우
            if (planetData.get(myPlayer.getPosition()).getCount() == 0) { // 빈땅일때
               setFlag(myPlayer.getPosition(), myPlayer);
               Message.setText("구매 완료하였습니다.");
               if (owner.equals("X")) {
                  setMoney(-price, 0);
               } else {
                  setMoney(-cost, 0);
               }

            } else if (planetData.get(myPlayer.getPosition()).getCount() > 3) {
               Alert alert = new Alert(AlertType.WARNING);
               alert.setTitle("알림");
               alert.setHeaderText("구매 불가능");
               alert.setContentText("이미 지을 수 있는 빌딩 개수의최대입니다.");
               alert.showAndWait();
               cnt1 = 0;
               cnt2 = 0;
               return;
            } else {
               int locate[] = { -33, -10, 13 };
               setBuilding(myPlayer.getPosition(), locate[planetData.get(myPlayer.getPosition()).getCount() - 1],
                     myPlayer); // 건물 설치
               // 함수 호출
               Message.setText("구매 완료하였습니다.");
               setMoney(-(price / 2), 0);
            }
         }

      }
      CostPlanet.setVisible(false);
      BuyPlanet.setVisible(false);
      Scene scene = new Scene(anchorPane);
      dialog.setScene(scene);
      dialog.show();
      checkdialog.add(dialog);
   }

   // -----------------------이동 관련 함수 시작------------------------------------
   public void setPosition(PlayerData player) {
      int cntX = player.getCntX();
      int cntY = player.getCntY();
      int posi = player.getPosi();
      int position = player.getPosition();
      path.getElements().add(new MoveTo(cntX, cntY));
      // 이동경로 설정
      if (posi == BOTTOM) {
         path.getElements().add(new HLineTo(cntX - 83));
         cntX -= 83;

         if (cntX == -732) {
            posi = LEFT;
         }
      } else if (posi == LEFT) {
         path.getElements().add(new VLineTo(cntY - 83));
         cntY -= 83;
         if (cntY == -395) {
            posi = TOP;
         }

      } else if (posi == TOP) {
         path.getElements().add(new HLineTo(cntX + 83));
         cntX += 83;
         if (cntX == 15) {
            posi = RIGHT;
         }
      } else if (posi == RIGHT) {
         path.getElements().add(new VLineTo(cntY + 83));
         cntY += 83;
         if (cntY == 20) {
            posi = BOTTOM;
         }
      }
      // 지구 위치를 지날때 설정
      if (position == pieceXY.size() - 1 && player.getTurn() == myPlayer.getTurn()) {
         setMoney(200000, 0);
      }
      position++; // 포지션 설정
      // 지구 위치를 지날때 설정
      if (position == pieceXY.size()) {
         position = 0;

      }
      player.setCntX(cntX);
      player.setCntY(cntY);
      player.setPosi(posi);
      player.setPosition(position);
   }

   // 비밀 카드 상황에 따른 이동 메서드.
   public void setPosition2(int move, PlayerData player) {
      if (Cardposi == 1) { // 5번째 카드, 조난 기지로.
         if (move <= 23) {
            move = 23 - move;
         } else {
            move = 51 - move;
         }
      } else if (Cardposi == 2) {// 7번째 카드 블랙홀로
         if (move < 14) {
            move = 14 - move;
         } else {
            move = move + 14 - (move - 14) * 2;
         }
      } else if (Cardposi == 3) {
         move = 28 - move;
      } else if (Cardposi == 4) {
         if (move < 9) {
            move = 9 - move;
         } else {
            move = 37 - move;
         }
      } else if (Cardposi == 5) {
         if (move <= 23) {
            move = 23 - move;
         } else {
            move = 26;
         }

      } else if (Cardposi == 6) {
         move = 5;
      }
      int movecount = move;
      Thread thread = new Thread() {
         @Override
         public void run() {
            SocketConnect.getOutMsg().println("MovePiece/" + player.getTurn() + "/" + movecount);
            path = new Path();
            Platform.runLater(() -> {

               for (int i = 0; i < movecount; i++) {
                  setPosition(player);
               }
               pathTransition.setDuration(Duration.millis(150 + 50 * (movecount)));
               pathTransition.setPath(path);
               if (player.getTurn() == 0) {
                  pathTransition.setNode(C1);
               } else {
                  pathTransition.setNode(C2);
               }
               pathTransition.play();

            });
            try {
               Thread.sleep(150 + 50 * (movecount) + 300);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            if (Cardposi == 4 || Cardposi == 6) {
               Platform.runLater(() -> {
                  if (myPlayer.getCard() > 0) {
                     if (planetData.get(myPlayer.getPosition()).getOwner().equals(yourPlayer.getId())) {
                        pass();
                     } else {
                        showDialog();
                     }
                  } else {
                     showDialog();
                  }
               });
            }

         }
      };
      thread.setDaemon(true);
      thread.start();
   }

   // 워프 다이얼로그 함수
   public void setWarp(AnchorPane anchorPane, Stage dialog) {
      // btn1.setDisable(true);
      Button button = new Button();
      Label label = new Label();
      ChoiceBox<String> choiceBox = new ChoiceBox<String>();
      label.setText("이동할 위치 선택 : ");
      label.setLayoutX(170);
      label.setLayoutY(200);

      choiceBox.setLayoutX(280);
      choiceBox.setLayoutY(195);
      choiceBox.setPrefWidth(100);

      int cnt = 1;
      for (int i = 0; i < planetData.size(); i++) {
         if (planetData.get(i).getName().equals("비밀카드")) {
            choiceBox.getItems().add(planetData.get(i).getName() + cnt++);
         } else if (planetData.get(i).getName().equals("워프")) {
            choiceBox.getItems().add(planetData.get(i).getName() + " 이동X");
         } else {
            choiceBox.getItems().add(planetData.get(i).getName());
         }

      }
      choiceBox.setValue("워프 이동X");
      button.setLayoutX(160);
      button.setLayoutY(250);
      button.setPrefWidth(100);
      button.setText("선택");
      anchorPane.getChildren().add(button);
      anchorPane.getChildren().add(choiceBox);
      anchorPane.getChildren().add(label);
      button.setOnAction(event -> getChoiceWarp(choiceBox, dialog));

   }

   // 워프 동작 함수
   private void getChoiceWarp(ChoiceBox<String> choiceBox, Stage dialog) {
      int index = choiceBox.getItems().indexOf(choiceBox.getValue());
      int move = 0;
      // 워프로 선택한 위치까지의 거리 계산
      if (index >= myPlayer.getPosition()) {
         move = index - myPlayer.getPosition();
      } else {
         move = 19 + index;
      }
      int movecount = move; // 거리 셋팅
      System.out.println(move);
      dialog.close();
      System.out.println(myPlayer.getPosition());
      Thread thread = new Thread() {
         @Override
         public void run() {
            path = new Path();
            Platform.runLater(() -> {
               // 상대방에게 말의 움직임을 서버를 통해 알려줌
               SocketConnect.getOutMsg().println("MovePiece/" + myPlayer.getTurn() + "/" + movecount);
               movePiece(movecount, myPlayer);
            });
            try {
               Thread.sleep(150 + 50 * (movecount) + 300);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            // 워프 이동시 다이얼로그 표시
            Platform.runLater(() -> {

               showDialog();
            });

         }

      };
      thread.setDaemon(true);
      thread.start();

   }

   // ---------------------------기본 셋팅 시작 -----------------------------------

   public void setRoot(AnchorPane root) {
      this.root = root;
   }

   public void setPrimaryStage(Stage primaryStage) {
      this.primaryStage = primaryStage;

   }

   public void setSocket(PlayerClient SocketConnect) {
      this.SocketConnect = SocketConnect;
   }

   public AppController() {
      pieceXY.add(new PieceXY(740, 505));
      pieceXY.add(new PieceXY(655, 505));
      pieceXY.add(new PieceXY(572, 505));
      pieceXY.add(new PieceXY(487, 505));
      pieceXY.add(new PieceXY(405, 505));
      pieceXY.add(new PieceXY(320, 505));
      pieceXY.add(new PieceXY(237, 505));
      pieceXY.add(new PieceXY(155, 505));
      pieceXY.add(new PieceXY(70, 505));
      pieceXY.add(new PieceXY(70, 425));
      pieceXY.add(new PieceXY(70, 348));
      pieceXY.add(new PieceXY(70, 266));
      pieceXY.add(new PieceXY(70, 186));
      pieceXY.add(new PieceXY(70, 105));
      pieceXY.add(new PieceXY(155, 105));
      pieceXY.add(new PieceXY(237, 105));
      pieceXY.add(new PieceXY(320, 105));
      pieceXY.add(new PieceXY(405, 105));
      pieceXY.add(new PieceXY(487, 105));
      pieceXY.add(new PieceXY(572, 105));
      pieceXY.add(new PieceXY(655, 105));
      pieceXY.add(new PieceXY(740, 105));
      pieceXY.add(new PieceXY(822, 105));
      pieceXY.add(new PieceXY(822, 186));
      pieceXY.add(new PieceXY(822, 266));
      pieceXY.add(new PieceXY(822, 348));
      pieceXY.add(new PieceXY(822, 425));
      pieceXY.add(new PieceXY(822, 505));

      planetData.add(new PlanetData("지구", "X", "수고비 20만원", 0, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("화성", "X", "20만원", 200000, 0));
      planetData.add(new PlanetData("목성", "X", "15만원", 150000, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("토성", "X", "8만원", 80000, 0));
      planetData.add(new PlanetData("천왕성", "X", "13만원", 130000, 0));
      planetData.add(new PlanetData("해왕성", "X", "25만원", 250000, 0));
      planetData.add(new PlanetData("물병자리", "X", "12만원", 120000, 0));
      planetData.add(new PlanetData("워프", "X", "이동", 0, 0));
      planetData.add(new PlanetData("명왕성", "X", "40만원", 400000, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("안드로메다", "X", "45만원", 450000, 0));
      planetData.add(new PlanetData("북극성", "X", "44만원", 440000, 0));
      planetData.add(new PlanetData("블랙홀", "X", "2턴 멈춤", 0, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("달", "X", "100만원", 1000000, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("태양", "X", "120만원", 1200000, 0));
      planetData.add(new PlanetData("시리우스", "X", "90만원", 900000, 0));
      planetData.add(new PlanetData("프로키온", "X", "95만원", 950000, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("베크록스", "X", "80만원", 800000, 0));
      planetData.add(new PlanetData("조난기지", "X", "1턴멈춤 기부 50만원", 0, 0));
      planetData.add(new PlanetData("아크록스", "X", "65만원", 650000, 0));
      planetData.add(new PlanetData("비밀카드", "X", "X", 0, 0));
      planetData.add(new PlanetData("수성", "X", "55만원", 550000, 0));
      planetData.add(new PlanetData("금성", "X", "60만원", 600000, 0));

   }

}