package com.offsec.nethunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

import com.offsec.nethunter.utils.ShellExecuter;
import com.offsec.nethunter.BlueNMEA.TCPServer;

import java.text.DateFormat;
import java.util.Date;


public class KismetFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "KaliLauncherFragment";
    ShellExecuter exe = new ShellExecuter();
    int upColor, downColor;
    private String gpsString="(socat TCP:127.0.0.1:4352 PTY,link=/tmp/gps & gpsd -n /tmp/gps) & ";
    Switch gpsSwitch;
    Switch packetSwitch;

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

        gpsSwitch = (Switch) rootView.findViewById(R.id.enablegps);
        packetSwitch = (Switch) rootView.findViewById(R.id.enablepacketlogging);
        Button launch = (Button) rootView.findViewById(R.id.launchkismetbutton);
        EditText configPath = (EditText) rootView.findViewById(R.id.configPath);
        final RadioGroup gpsSource = (RadioGroup) rootView.findViewById(R.id.provider);

        final SharedPreferences prefs = getContext().getSharedPreferences("kismetPrefs", Context.MODE_PRIVATE);
        gpsSwitch.setChecked(prefs.getBoolean("enableGps", false));
        packetSwitch.setChecked(prefs.getBoolean("packetLog", false));


        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("enableGps", isChecked);
                prefs.edit().apply();
                if(isChecked)
                    gpsSource.setVisibility(View.VISIBLE);
                else
                    gpsSource.setVisibility(View.INVISIBLE);
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
            wlanStat.setText("Wlan1 is up");
            wlanStat.setTextColor(upColor);
        } else {
            wlanStat.setText("Wlan1 is not found");
            wlanStat.setTextColor(downColor);
        }
    }

    public void launchKismet(){
        String launchstring = "kismet -g -t Kismet-" + DateFormat.getDateTimeInstance().format(new Date());

        if(gpsSwitch.isChecked()) {
            launchstring = gpsString + launchstring;
            try {
                Intent intent = new Intent("name.kellermann.max.bluenmea.MAIN");
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_BlueNMEA), Toast.LENGTH_SHORT).show();
            }
        }

        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("com.offsec.nhterm.iInitialCommand", launchstring);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_install_terminal), Toast.LENGTH_SHORT).show();
        }
    }
}