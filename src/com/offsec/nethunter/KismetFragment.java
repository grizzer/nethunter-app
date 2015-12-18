package com.offsec.nethunter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

import com.offsec.nethunter.utils.ShellExecuter;


public class KismetFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "KaliLauncherFragment";
    ShellExecuter exe = new ShellExecuter();
    int upColor, downColor;

    public KismetFragment() {

    }

    public static KismetFragment newInstance(int sectionNumber) {
        KismetFragment fragment = new KismetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.kismet, container, false);
        upColor = ContextCompat.getColor(getContext(), R.color.wlan1Up);
        downColor = ContextCompat.getColor(getContext(), R.color.wlan1Down);

        setWlan1Stat(rootView);

        Switch gpsSwitch = (Switch) rootView.findViewById(R.id.enablegps);
        Switch packetSwitch = (Switch) rootView.findViewById(R.id.enablepacketlogging);
        Button launch = (Button) rootView.findViewById(R.id.launchkismetbutton);
        EditText configPath = (EditText) rootView.findViewById(R.id.configPath);

        final SharedPreferences prefs = getContext().getSharedPreferences("kismetPrefs", Context.MODE_PRIVATE);
        gpsSwitch.setChecked(prefs.getBoolean("enableGps", false));
        packetSwitch.setChecked(prefs.getBoolean("packetLog", false));


        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("enableGps", isChecked);
            }
        });

        packetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                prefs.edit().putBoolean("packetLog", isChecked);
            }
        });

        launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchKismet();
            }
        });

        configPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(verifyFile(s.toString())){
                    prefs.edit().putString("configPath", s.toString());
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Invalid config file: " + s.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        return rootView;
    }

    private Boolean verifyFile(String filepath){
        return exe.RunAsRootOutput("[ -f " + filepath + " ] && echo \"\" || echo \"Not Found\" ").isEmpty();
    }

    private void setWlan1Stat(View rootView){
        Boolean wlan1 = !(exe.RunAsRootOutput("ip addr | grep wlan1").isEmpty());
        TextView wlanStat = (TextView) rootView.findViewById(R.id.wlan1status);
        if(wlan1){
            wlanStat.setText("Wlan1 is Up");
            wlanStat.setTextColor(upColor);
        } else {
            wlanStat.setText("Wlan1 is not found");
            wlanStat.setTextColor(downColor);
        }
    }

    public void launchKismet(){
        Toast toast = Toast.makeText(getContext(), "Kismet To-Do", Toast.LENGTH_SHORT);
        toast.show();
    }
}
