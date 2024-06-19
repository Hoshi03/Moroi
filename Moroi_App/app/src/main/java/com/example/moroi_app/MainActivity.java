package com.example.moroi_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    static class ObstacleButton {
        public int x = 0;
        public int y = 0;
        public int obstacleRemainTime = 0;

        ObstacleButton(int x, int y, int obstacleRemainTime) {
            this.x = x;
            this.y = y;
            this.obstacleRemainTime = obstacleRemainTime;
        }
    }
    static class PredictButton {
        public int x = 0;
        public int y = 0;
        public int predictRemainTime = 0;
        public int player = 0;

        PredictButton(int x, int y, int predictRemainTime, int player) {
            this.x = x;
            this.y = y;
            this.predictRemainTime = predictRemainTime;
            this.player = player;
        }
    }

    // 지금은 상수로 행,열 박아뒀는데 서버넣으면 겜 시작할때 난이도 선택하면 조절 가능하게 한 다음
    // oncreate에서 넣어주는걸로 수정하면 될것같습니다
    
    private static final int ROWS = 7;
    private static final int COLS = 13;
    private static final int obstacleRemainTime = 5; // 5로햐여  p1이 설치 - p2턴 3턴지남 장애물 해제 가능
    private static final int predictRemainTime = 2;

    private static boolean[][] obstacleButtons = new boolean[ROWS][COLS];
    private static boolean[][] predictButtons = new boolean[ROWS][COLS];

    private static int currentTurn = 1;
    private TextView playerPrompt;
    private TextView turnCount;

    private ArrayList<ObstacleButton> obstacleButtonList;
    private ArrayList<PredictButton> predictButtonList;
    private Button[][] buttons = new Button[ROWS][COLS];
    private int currentPlayer = 1;
    private boolean moveMode = false;
    private boolean blockMode = false;
    private boolean predictMode = false;

    private int p1Row = 0, p1Col = 0, p2Row = ROWS - 1, p2Col = COLS - 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerPrompt = findViewById(R.id.playerPrompt);
        turnCount = findViewById(R.id.turnCount);
        initializeBoard();
        updatePlayerUI();
        obstacleButtonList = new ArrayList<>();
        predictButtonList = new ArrayList<>();

        ImageButton moveButton = findViewById(R.id.moveButton);
        moveButton.setOnClickListener(v -> {
            moveMode = true;
            blockMode = false;
            predictMode = false;
        });

        ImageButton blockButton = findViewById(R.id.blockButton);
        blockButton.setOnClickListener(v -> {
            blockMode = true;
            moveMode = false;
            predictMode = false;
        });

        ImageButton predictButton = findViewById(R.id.predictButton);
        predictButton.setOnClickListener(v -> {
            predictMode = true;
            blockMode = false;
            moveMode = false;
        });
    }

    //체스판처럼 만들어두기, 안드에 있는거 썻습니다
    private void initializeBoard() {
        TableLayout boardLayout = findViewById(R.id.boardLayout);

        for (int i = 0; i < ROWS; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1));

            for (int j = 0; j < COLS; j++) {
                Button button = new Button(this);
                button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1));

                if ((i + j) % 2 == 0) button.setBackgroundColor(Color.WHITE);
                else button.setBackgroundColor(Color.BLACK);

                button.setOnClickListener(new ButtonClickListener(i, j));
                row.addView(button);
                buttons[i][j] = button;
            }

            boardLayout.addView(row);
        }

        buttons[0][0].setBackgroundColor(Color.BLUE);
        buttons[ROWS-1][COLS-1].setBackgroundColor(Color.RED);
    }

    private class ButtonClickListener implements View.OnClickListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            if (moveMode && isValidMove(row, col) && !obstacleButtons[row][col]) {
                updatePlayerPos(row, col);
                moveMode = false;
            } else if (blockMode) {
                placeObstacle(row, col);
                blockMode = false;
            } else if (predictMode) {
                placePrediction(row, col);
                predictMode = false;
            }
        }
    }

    //지금은 색만 바뀌는데 색 바꿀때 해당 좌표로 테미 goto 보내면 바로 적용될거같습니다
    private void updatePlayerPos(int row, int col) {
        updateObstacleLifeTime();
        updatePredictionLifeTime();
        updateTurnCount();
        if (currentPlayer == 1) currentTurn++;

        if (currentPlayer == 1) {
            if ((p1Row + p1Col) % 2 == 0) {
                buttons[p1Row][p1Col].setBackgroundColor(Color.WHITE);
            } else {
                buttons[p1Row][p1Col].setBackgroundColor(Color.BLACK);
            }
        } else {
            if ((p2Row + p2Col) % 2 == 0) {
                buttons[p2Row][p2Col].setBackgroundColor(Color.WHITE);
            } else {
                buttons[p2Row][p2Col].setBackgroundColor(Color.BLACK);
            }
        }

        // currentPlayer가 1인지 2인지에 따라 플레이어 "row,col" 로 goto
        int color = currentPlayer == 1 ? Color.BLUE : Color.RED;
        buttons[row][col].setBackgroundColor(color);

        
        //p1이 p2 잡은 경우 p2를 맵 우하단으로 goto
        if (currentPlayer == 1) {
            p1Row = row;
            p1Col = col;
            if (row == p2Row && col == p2Col) {
                p2Row = ROWS - 1;
                p2Col = COLS - 1;
                buttons[p2Row][p2Col].setBackgroundColor(Color.RED);
            }

            // p1이 p2 시작점에 도달 겜끝
            if (row == ROWS - 1 && col == COLS - 1) {
                showVictoryMessage(1);
                return;
            }
        }
        //p2가 p1 잡은 경우 p1를 멥 좌상단으로 goto
        else {
            p2Row = row;
            p2Col = col;
            if (row == p1Row && col == p1Col) {
                p1Row = 0;
                p1Col = 0;
                buttons[p1Row][p1Col].setBackgroundColor(Color.BLUE);
            }

            // p2가 p1 시작점에 도달 겜끝
            if (row == 0 && col == 0) {
                showVictoryMessage(2);
                return;
            }
        }

        for (Iterator<PredictButton> iterator = predictButtonList.iterator(); iterator.hasNext();) {
            PredictButton pb = iterator.next();
            if (pb.x == row && pb.y == col) {
                if (pb.player == 1 && currentPlayer == 2) {
                    p2Row = ROWS - 1;
                    p2Col = COLS -1;
                    //p1이 p2 예측한 경우
                    buttons[p2Row][p2Col].setBackgroundColor(Color.RED);
                }

                else if (pb.player == 2 && currentPlayer == 1) {
                    p1Row = 0;
                    p1Col = 0;
                    buttons[p1Row][p1Col].setBackgroundColor(Color.BLUE);
                }

                iterator.remove();
                predictButtons[row][col] = false;
                if ((row + col) % 2 == 0) {
                    buttons[row][col].setBackgroundColor(Color.WHITE);
                } else {
                    buttons[row][col].setBackgroundColor(Color.BLACK);
                }
                break;
            }
        }
        currentPlayer = currentPlayer == 1 ? 2 : 1;
        updatePlayerUI();
    }

    private boolean isValidMove(int row, int col) {
        int validRow, validCol;

        if (currentPlayer == 1) {
            validRow = Math.abs(row - p1Row);
            validCol = Math.abs(col - p1Col);
        } else {
            validRow = Math.abs(row - p2Row);
            validCol = Math.abs(col - p2Col);
        }
        return validRow <= 1 && validCol <= 1;
    }
    //장애믈 설치

    private void placeObstacle(int row, int col) {
        updateObstacleLifeTime();
        obstacleButtonList.add(new ObstacleButton(row, col, obstacleRemainTime));
        Button button = buttons[row][col];

        updateTurnCount();
        if (currentPlayer == 1) currentTurn++;
        obstacleButtons[row][col] = true;

        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle);
        int buttonWidth = button.getWidth();
        int buttonHeight = button.getHeight();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, true);
        button.setBackground(new BitmapDrawable(getResources(), scaledBitmap));

        currentPlayer = currentPlayer == 1 ? 2 : 1;
        updatePlayerUI();
    }

    //예측
    private void placePrediction(int row, int col) {
        updatePredictionLifeTime();
        predictButtonList.add(new PredictButton(row, col, predictRemainTime, currentPlayer));
        predictButtons[row][col] = true;
        //서버 넣으면 예측한 사람만 그 칸 노란색으로 보이게 바꾸겠습니다
        /*buttons[row][col].setBackgroundColor(Color.YELLOW);*/
        currentPlayer = currentPlayer == 1 ? 2 : 1;
        updatePlayerUI();
    }

    
    // 턴, 현재 플레이어 등 ui 갱신
    private void updatePlayerUI() {
        String s = currentPlayer == 1 ? "Player1의 다음 행동을 선택해주세요" : "Player2의 다음 행동을 선택해주세요";
        playerPrompt.setText(s);
    }

    private void updateTurnCount() {
        String s = "Turn : " + currentTurn;
        turnCount.setText(s);
    }

    // 장애물 생명주기
    private void updateObstacleLifeTime() {
        Iterator<ObstacleButton> iterator = obstacleButtonList.iterator();
        while (iterator.hasNext()) {
            ObstacleButton x = iterator.next();
            if (x.obstacleRemainTime > 0) x.obstacleRemainTime--;
            if (x.obstacleRemainTime == 0) {
                if ((x.x + x.y) % 2 == 0) buttons[x.x][x.y].setBackgroundColor(Color.WHITE);
                else buttons[x.x][x.y].setBackgroundColor(Color.BLACK);
                obstacleButtons[x.x][x.y] = false;
                iterator.remove();
            }
        }
    }

    // 예측 생명주기
    private void updatePredictionLifeTime() {
        Iterator<PredictButton> iterator = predictButtonList.iterator();
        while (iterator.hasNext()) {
            PredictButton x = iterator.next();
            if (x.predictRemainTime > 0) x.predictRemainTime--;
            if (x.predictRemainTime == 0) {
                if ((x.x + x.y) % 2 == 0) buttons[x.x][x.y].setBackgroundColor(Color.WHITE);
                else buttons[x.x][x.y].setBackgroundColor(Color.BLACK);
                predictButtons[x.x][x.y] = false;
                iterator.remove();
            }
        }
    }


    private void showVictoryMessage(int player) {
        disableAllButtons();

        if (player == 1){
            Intent intent = new Intent(MainActivity.this, P1Win.class);
            startActivity(intent);
            finish();
        }

        else  {
            Intent intent = new Intent(MainActivity.this, P2Win.class);
            startActivity(intent);
            finish();
        }

        String message = player == 1 ? "Player 1 Wins!" : "Player 2 Wins!";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //겜끝나면 버튼 다 못누르게 만들기
    private void disableAllButtons() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }
}

