package rokudol.com.curvechart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aaa);
        CurveChart curveChart = findViewById(R.id.chart);
        List xCoordinateData = new ArrayList<>();
        xCoordinateData.add("区域一");
        xCoordinateData.add("区域二");
        xCoordinateData.add("区域三");
        xCoordinateData.add("区域四");
        xCoordinateData.add("区域五");
        xCoordinateData.add("区域六");
        xCoordinateData.add("区域七");
        xCoordinateData.add("区域八");

        List yCoordinateData = new ArrayList<>();
        yCoordinateData.add(40.54);
        yCoordinateData.add(70.54);
        yCoordinateData.add(50.38);
        yCoordinateData.add(60.54);
        yCoordinateData.add(13.76);
        yCoordinateData.add(60.38);
        yCoordinateData.add(34.67);
        yCoordinateData.add(45.78);

        try {
            curveChart.setData(xCoordinateData,yCoordinateData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
