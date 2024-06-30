package com.example.javatestwithjson;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer timer;
    private static final int QUIZ_DURATION = 300000*12;
    private static final String TAG = "MainActivity";

    private LinearLayout startScreen, quizScreen, resultScreen;
    private TextView questionTextView, codeTextView, questionNumberTextView, scoreTextView, gradeTextView, timerTextView;
    private LinearLayout optionGroup;
    private CheckBox option1, option2, option3, option4;
    private Button nextButton, restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScreen = findViewById(R.id.startScreen);
        quizScreen = findViewById(R.id.quizScreen);
        resultScreen = findViewById(R.id.resultScreen);

        questionTextView = findViewById(R.id.question);
        codeTextView = findViewById(R.id.code);
        questionNumberTextView = findViewById(R.id.questionNumber);
        optionGroup = findViewById(R.id.optionGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        restartButton = findViewById(R.id.restartButton);

        scoreTextView = findViewById(R.id.score);
        gradeTextView = findViewById(R.id.grade);
        timerTextView = findViewById(R.id.timerTextView);

        loadQuestions();
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();
        try {
            InputStream is = getResources().openRawResource(R.raw.questions);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObject = questionsArray.getJSONObject(i);
                String question = questionObject.getString("question");
                String code = questionObject.optString("code", null);
                JSONArray optionsArray = questionObject.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }
                JSONArray answersArray = questionObject.getJSONArray("answer");
                List<Integer> answers = new ArrayList<>();
                for (int j = 0; j < answersArray.length(); j++) {
                    answers.add(answersArray.getInt(j));
                }
                questionList.add(new Question(question, code, options, answers));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading questions", e);
        }
    }

    public void startQuiz(View view) {
        Collections.shuffle(questionList);
        currentQuestionIndex = 0;
        score = 0;
        startScreen.setVisibility(View.GONE);
        quizScreen.setVisibility(View.VISIBLE);
        resultScreen.setVisibility(View.GONE);
        displayQuestion();

        timer = new CountDownTimer(QUIZ_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                endQuiz();
            }
        }.start();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.getQuestion());
            if (currentQuestion.getCode() != null && !currentQuestion.getCode().isEmpty()) {
                codeTextView.setText(currentQuestion.getCode());
                codeTextView.setVisibility(View.VISIBLE);
            } else {
                codeTextView.setVisibility(View.GONE);
            }
            questionNumberTextView.setText(String.format("Question %d/%d", currentQuestionIndex + 1, questionList.size()));
            List<String> options = currentQuestion.getOptions();
            option1.setText(options.get(0));
            option2.setText(options.get(1));
            option3.setText(options.get(2));
            option4.setText(options.get(3));
            option1.setChecked(false);
            option2.setChecked(false);
            option3.setChecked(false);
            option4.setChecked(false);

            if (currentQuestionIndex == questionList.size() - 1) {
                nextButton.setText("Zakończ test");
            } else {
                nextButton.setText("Następne pytanie");
            }
        }
    }

    public void nextQuestion(View view) {
        List<Integer> selectedAnswers = new ArrayList<>();
        if (option1.isChecked()) selectedAnswers.add(0);
        if (option2.isChecked()) selectedAnswers.add(1);
        if (option3.isChecked()) selectedAnswers.add(2);
        if (option4.isChecked()) selectedAnswers.add(3);

        Question currentQuestion = questionList.get(currentQuestionIndex);
        List<Integer> correctAnswers = currentQuestion.getAnswers();

        if (selectedAnswers.containsAll(correctAnswers) && correctAnswers.containsAll(selectedAnswers)) {
            score++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            displayQuestion();
        } else {
            endQuiz();
        }
    }

    private void endQuiz() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            quizScreen.setVisibility(View.GONE);
            resultScreen.setVisibility(View.VISIBLE);
            scoreTextView.setText("Twój wynik: " + score + " / " + questionList.size());

            int totalQuestions = questionList.size();
            double percentage = ((double) score / totalQuestions) * 100;
            String grade;
            if (percentage < 60) {
                grade = "2.0";
            } else if (percentage >=60&& percentage< 65) {
                grade = "3.0";
            } else if (percentage >=65&& percentage< 70) {
                grade = "3.5";
            } else if (percentage >=70&& percentage< 80) {
                grade = "4.0";
            } else if (percentage >=80&& percentage< 90) {
                grade = "4.5";
            } else {
                grade = "5.0";
            }
            gradeTextView.setText("Twoja ocena: " + grade);

            saveResults();
        } catch (Exception e) {
            Log.e(TAG, "Error ending quiz", e);
        }
    }

    private void saveResults() {
        try {
            JSONArray resultsArray = new JSONArray();
            JSONObject resultObject = new JSONObject();
            resultObject.put("score", score);
            resultObject.put("total", questionList.size());
            resultsArray.put(resultObject);

            String resultsJson = resultsArray.toString();
            FileOutputStream fos = openFileOutput("results.json", Context.MODE_PRIVATE);
            fos.write(resultsJson.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saving results", e);
        }
    }

    public void restartQuiz(View view) {
        startScreen.setVisibility(View.VISIBLE);
        quizScreen.setVisibility(View.GONE);
        resultScreen.setVisibility(View.GONE);
    }

    private static class Question {
        private final String question;
        private final String code;
        private final List<String> options;
        private final List<Integer> answers;

        public Question(String question, String code, List<String> options, List<Integer> answers) {
            this.question = question;
            this.code = code;
            this.options = options;
            this.answers = answers;
        }

        public String getQuestion() {
            return question;
        }

        public String getCode() {
            return code;
        }

        public List<String> getOptions() {
            return options;
        }

        public List<Integer> getAnswers() {
            return answers;
        }
    }
}
