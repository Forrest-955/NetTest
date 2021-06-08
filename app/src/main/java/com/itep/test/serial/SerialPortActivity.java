package com.itep.test.serial;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bjw.bean.ComBean;
import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;
import com.itep.test.R;
import com.itep.test.Utils;

import java.io.IOException;

import android_serialport_api.SerialPortFinder;

public class SerialPortActivity extends AppCompatActivity {

    private static final int SENDTXT = 0;
    private static final int SENDHEX = 1;
    private static final int SETTEXT = 2;
    private RecyclerView recy;
    private Spinner spSerial;
    private EditText edInput;
    private Button btSend;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private SerialPortFinder serialPortFinder;
    private SerialHelper serialHelper;
    private Spinner spBote;
    private Button btOpen;
    private Button btnRoundSend;
    private Button btnStop;
    private Button btnClose;
    private EditText etTime;
    private TextView tvNum;
    private LogListAdapter logListAdapter;
    private long delay = 1000;
    private int sendCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seria_port_helper);

        recy = (RecyclerView) findViewById(R.id.recy);
        spSerial = (Spinner) findViewById(R.id.sp_serial);
        edInput = (EditText) findViewById(R.id.ed_input);
        btSend = (Button) findViewById(R.id.bt_send);
        spBote = (Spinner) findViewById(R.id.sp_bote);
        btOpen = (Button) findViewById(R.id.bt_open);
        btnRoundSend = findViewById(R.id.bt_autosend);
        btnStop = findViewById(R.id.bt_stop);
        btnClose = findViewById(R.id.bt_close);
        etTime = findViewById(R.id.et_time);
        tvNum = findViewById(R.id.tv_sendnum);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);


        logListAdapter = new LogListAdapter(null);

        recy.setLayoutManager(new LinearLayoutManager(this));
        recy.setAdapter(logListAdapter);
        recy.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        iniview();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHelper.close();
    }

    private void iniview() {


        serialPortFinder = new SerialPortFinder();
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), FuncUtil.ByteArrToHex(comBean.bRec), Toast.LENGTH_SHORT).show();
//                        logListAdapter.addData(comBean.sRecTime+":   "+FuncUtil.ByteArrToHex(comBean.bRec));
                        logListAdapter.addData(comBean.sRecTime+":   "+new String(comBean.bRec));
                        recy.smoothScrollToPosition(logListAdapter.getData().size());
                    }
                });
            }
        };

        final String[] ports = serialPortFinder.getAllDevicesPath();
        final String[] botes = new String[]{"0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000"};

        SpAdapter spAdapter = new SpAdapter(this);
        spAdapter.setDatas(ports);
        spSerial.setAdapter(spAdapter);

        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialHelper.close();
                serialHelper.setPort(ports[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter2 = new SpAdapter(this);
        spAdapter2.setDatas(botes);
        spBote.setAdapter(spAdapter2);


        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialHelper.close();
                serialHelper.setBaudRate(botes[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serialHelper.open();
                    btOpen.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            serialHelper.sendTxt(edInput.getText().toString());
                        } else {
                            Toast.makeText(getBaseContext(), "串口未打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "发送数据为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            serialHelper.sendHex(edInput.getText().toString());
                        } else {
                            Toast.makeText(getBaseContext(), "串口未打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "发送数据为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnRoundSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCount = 0;
                delay = Long.parseLong(etTime.getText().toString());
                btnRoundSend.setEnabled(false);
                btSend.setEnabled(false);
                btnStop.setEnabled(true);
                edInput.setEnabled(false);
                radioButton1.setEnabled(false);
                radioButton2.setEnabled(false);
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            handler.sendEmptyMessageDelayed(SENDTXT, delay);
                        } else {
                            Toast.makeText(getBaseContext(), "串口未打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "发送数据为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            handler.sendEmptyMessageDelayed(SENDHEX, Long.parseLong(etTime.getText().toString()));
                        } else {
                            Toast.makeText(getBaseContext(), "串口未打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "发送数据为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRoundSend.setEnabled(true);
                btnStop.setEnabled(false);
                btSend.setEnabled(true);
                edInput.setEnabled(true);
                radioButton1.setEnabled(true);
                radioButton2.setEnabled(true);
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    handler.removeMessages(SENDTXT);
                } else {
                    handler.removeMessages(SENDHEX);
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    handler.removeMessages(SENDTXT);
                } else {
                    handler.removeMessages(SENDHEX);
                }
                finish();
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SENDTXT:
                    serialHelper.sendTxt(edInput.getText().toString());
                    sendCount++;
                    setText();
                    logListAdapter.addData(System.currentTimeMillis()+":   "+edInput.getText().toString());
                    handler.sendEmptyMessageDelayed(SENDTXT, delay);
                    break;
                case SENDHEX:
                    serialHelper.sendHex(edInput.getText().toString());
                    sendCount++;
                    setText();
                    logListAdapter.addData(System.currentTimeMillis()+":   "+ new String(Utils.hexToBytes(edInput.getText().toString())));
                    handler.sendEmptyMessageDelayed(SENDHEX, delay);
                    break;
                case SETTEXT:
                    String result = String.format("已循环发送%d条数据", sendCount);
                    tvNum.setText(result);
                    Utils.writeToFile(Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/serial.txt", result);
                    break;
            }
            return false;
        }
    });

    private void setText() {
        Message msg = new Message();
        msg.what = SETTEXT;
        handler.sendMessage(msg);
    }
}
