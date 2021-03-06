/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，
 * 也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package oneapp.onechat.chat.sharesdk.sms;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mob.tools.utils.ResHelper;

import java.util.ArrayList;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import oneapp.onechat.androidapp.R;
import oneapp.onechat.chat.view.BaseActivity;


/**
 * 国家列表界面
 */
public class CountryPage extends BaseActivity implements TextWatcher, GroupListView.OnItemClickListener, View.OnClickListener {
    private String id;
    // 国家号码规则
    private HashMap<String, String> countryRules;
    private EventHandler handler;
    private CountryListView listView;
    private EditText etSearch;
    private Dialog pd;
    public static final int INTENT_COUNTRY = 1;
    private HashMap<String, Object> result;
    private ImageView img_back;

    public final void setResult(HashMap<String, Object> result) {
        this.result = result;
    }

//	public void setCountryId(String id) {
//		this.id = id;
//	}
//
//	public void setCountryRuls(HashMap<String, String> countryRules) {
//		this.countryRules = countryRules;
//	}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.country_layout);
        Intent intent = getIntent();
        id = intent.getStringExtra("currentId");

        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = CommonDialog.ProgressDialog(this);
        if (pd != null) {
            pd.show();
        }
        // 初始化搜索引擎
        SearchEngine.prepare(this, new Runnable() {
            public void run() {
                afterPrepare();
            }
        });

    }

    private void afterPrepare() {
        runOnUiThread(new Runnable() {
            public void run() {
//				CountryListPageLayout page = new CountryListPageLayout(this);
//				LinearLayout layout = page.getLayout();
//
//				if (layout != null) {
//					activity.setContentView(layout);
//				}
//                setContentView(R.layout.country_layout);

                if (countryRules == null || countryRules.size() <= 0) {
                    handler = new EventHandler() {
                        @SuppressWarnings("unchecked")
                        public void afterEvent(int event, final int result, final Object data) {
                            if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (pd != null && pd.isShowing()) {
                                            pd.dismiss();
                                        }

                                        if (result == SMSSDK.RESULT_COMPLETE) {
                                            onCountryListGot((ArrayList<HashMap<String, Object>>) data);
                                        } else {
                                            ((Throwable) data).printStackTrace();
                                            int resId = ResHelper.getStringRes(CountryPage.this, "smssdk_network_error");
                                            if (resId > 0) {
                                                Toast.makeText(CountryPage.this, resId, Toast.LENGTH_SHORT).show();
                                            }
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    };
                    // 注册回调接口
                    SMSSDK.registerEventHandler(handler);
                    // 获取国家列表
                    SMSSDK.getSupportedCountries();
                } else {
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                    initPage();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initPage() {
//		activity.findViewById(ResHelper.getIdRes(activity, "ll_back")).setOnClickListener(this);
        findViewById(R.id.et_search).setOnClickListener(this);
//		activity.findViewById(ResHelper.getIdRes(activity, "iv_clear")).setOnClickListener(this);

        int resId = ResHelper.getIdRes(this, "clCountry");
        listView = (CountryListView) findViewById(resId);
        listView.setOnItemClickListener(this);

//		resId = ResHelper.getIdRes(this, "et_put_identify");
        etSearch = (EditText) findViewById(R.id.et_search);
        etSearch.addTextChangedListener(this);

        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);
        img_back.setVisibility(View.VISIBLE);
    }

    private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
        // 解析国家列表
        for (HashMap<String, Object> country : countries) {
            String code = (String) country.get("zone");
            String rule = (String) country.get("rule");
            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                continue;
            }

            if (countryRules == null) {
                countryRules = new HashMap<String, String>();
            }
            countryRules.put(code, rule);
        }
        // 回归页面初始化操作
        initPage();
    }

    public void onItemClick(GroupListView parent, View view, int group, int position) {
        if (position >= 0) {
            String[] country = listView.getCountry(group, position);
            if (countryRules != null && countryRules.containsKey(country[1])) {
                id = country[2];
                Intent intent = new Intent();
                intent.putExtra("page", 1);
                intent.putExtra("id", id);
                setResult(4, intent);
                finish();
            } else {
                int resId = ResHelper.getStringRes(this, "smssdk_country_not_support_currently");
                if (resId > 0) {
                    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_search:
                etSearch.getText().clear();
                etSearch.requestFocus();
                break;
            case R.id.img_back:
                finish();
                break;
        }
//		int id = v.getId();
//		int idLlBack = ResHelper.getIdRes(activity, "ll_back");
//		int idIvSearch = ResHelper.getIdRes(activity, "ivSearch");
//		int idIvClear = ResHelper.getIdRes(activity, "iv_clear");
//		if (id == idLlBack) {
//			finish();
//		} else if (id == idIvSearch) {
//			// 搜索
//			int idLlTitle = ResHelper.getIdRes(activity, "llTitle");
//			activity.findViewById(idLlTitle).setVisibility(View.GONE);
//			int idLlSearch = ResHelper.getIdRes(activity, "llSearch");
//			activity.findViewById(idLlSearch).setVisibility(View.VISIBLE);
//			etSearch.getText().clear();
//			etSearch.requestFocus();
//		} else if (id == idIvClear) {
//			etSearch.getText().clear();
//		}
    }

//	public boolean onKeyEvent(int keyCode, KeyEvent event) {
//		try {
//			int resId = ResHelper.getIdRes(this, "llSearch");
//			if (keyCode == KeyEvent.KEYCODE_BACK
//					&& event.getAction() == KeyEvent.ACTION_DOWN
//					&& findViewById(resId).getVisibility() == View.VISIBLE) {
//				findViewById(resId).setVisibility(View.GONE);
//				resId = ResHelper.getIdRes(this, "llTitle");
//				findViewById(resId).setVisibility(View.VISIBLE);
//				etSearch.setText("");
//				return true;
//			}
//		} catch (Throwable e) {
//			SMSLog.getInstance().w(e);
//		}
//		return onKeyEvent(keyCode, event);
//	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if(requestCode==INTENT_COUNTRY){
//
//        }
//	}


    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        listView.onSearch(s.toString().toLowerCase());
    }

    public void afterTextChanged(Editable s) {

    }

}
