package com.buzz.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.buzz.models.action;
import com.buzz.utils.GlobalConst;

@Deprecated
public class DownloadFileActivity extends ActionBarActivity {

    /*
    static final String TAG = "ASYNC_TASK";

    Button execute;
    Button cancel;
    ProgressBar progressBar;
    TextView txtResult;
    TextView txtDoneList;
    Map<String, MyTask> taskList;

    MyTask mTask;
    MyApplication myApp;
    int fileCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        myApp = (MyApplication) getApplication();
        taskList = new HashMap<String, MyTask>();

        execute = (Button) findViewById(R.id.execute);
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDoneList.setText("");
                taskList.clear();
                //注意每次需new一个实例,新建的任务只能执行一次,否则会出现异常
                for (List<action> acList : myApp.actionList.values()) {
                    for (action ac : acList) {
                        taskList.put(ac.getServerpath(), new MyTask(ac.getClientpath(), ac.getFilename()));
                    }
                }

                for (Map.Entry<String, MyTask> entry : taskList.entrySet()) {
                    entry.getValue().execute(entry.getKey());
                }

                execute.setEnabled(false);
                cancel.setEnabled(true);
            }
        });
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消一个正在执行的任务,onCancelled方法将会被调用
                mTask.cancel(true);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtDoneList = (TextView) findViewById(R.id.txtDoneList);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_download_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    private class MyTask extends AsyncTask<String, Integer, String> {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            //Log.i(TAG, "onPreExecute() called");
            txtResult.setText("准备下载...\n");
        }

        private String clientPath;
        private String fileName;

        protected MyTask(String clientPath, String fileName) {
            this.clientPath = clientPath;
            this.fileName = fileName;
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            //Log.i(TAG, "doInBackground(Params... params) called");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse response = client.execute(get);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    long total = entity.getContentLength();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int count = 0;
                    int length = -1;
                    while ((length = is.read(buf)) != -1) {
                        baos.write(buf, 0, length);
                        count += length;
                        //调用publishProgress公布进度,最后onProgressUpdate方法将被执行
                        publishProgress((int) ((count / (float) total) * 100));
                        //为了演示进度,休眠500毫秒
                        //Thread.sleep(500);
                    }

                    //保存文件
                    String filePath = GlobalConst.PATH_SDCARD + this.clientPath;
                    String fileName = this.fileName;
                    String saveTo = filePath + fileName;
                    File file = new File(filePath);
                    file.mkdirs();
                    file = null;
                    file = new File(saveTo);
                    file.createNewFile();
                    OutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(baos.toByteArray());
                    baos.close();
                    baos.flush();
                    outputStream.close();
                    outputStream.flush();
                    file = null;

                    return "[" + this.fileName + "]" + "=>[下载完成]\n";
                }
            } catch (Exception e) {
                //Log.i(TAG, e.getMessage());
            }
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
            //Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
            progressBar.setProgress(progresses[0]);
            txtResult.setText("[" + this.fileName + "]" + "=>[下载中..." + progresses[0] + "%]\n");
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            //Log.i(TAG, "onPostExecute(Result result) called");
            txtResult.setText(result);
            txtDoneList.append(result);

            fileCounter++;
            if (fileCounter == taskList.size()) {
                execute.setEnabled(true);
                cancel.setEnabled(false);
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
            //Log.i(TAG, "onCancelled() called");
            txtResult.setText("cancelled");
            progressBar.setProgress(0);

            execute.setEnabled(true);
            cancel.setEnabled(false);
        }
    }*/
}
