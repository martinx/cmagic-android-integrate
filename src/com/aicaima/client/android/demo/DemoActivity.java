package com.aicaima.client.android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 基于Zxing的IntentIntegrator修改
 *
 * @author Martin Xu
 */

public class DemoActivity extends Activity {
    private static final String TAG = "DemoActivity";
    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button btn = (Button) this.findViewById(R.id.btn);
        tv = (TextView) this.findViewById(R.id.result);
        btn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(DemoActivity.this);
                integrator.setPromptMessage("自定义提示信息");
                integrator.setUpdateFlag(false);
                //integrator.setPromptMessageByID();
                integrator.initiateScan();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (null != result) {
            StringBuilder builder = new StringBuilder();
            builder.append("编码：").append(result.getItemId()).append("\n")
                    .append("类型：").append(result.getFormatName()).append("\n")
                    .append("标题：").append(result.getTitle()).append("\n")
                    .append("内容：").append(result.getContent()).append("\n");
            tv.setText(builder.toString());

        } else {
            tv.setText("没有找到彩码资源");
        }

    }
}
