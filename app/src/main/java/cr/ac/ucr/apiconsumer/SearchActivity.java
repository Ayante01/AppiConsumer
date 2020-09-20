package cr.ac.ucr.apiconsumer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar tToolbar;
    private TextInputEditText etChangeLocation;
    public static final String KEY = "city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tToolbar = findViewById(R.id.t_toolbar);

        tToolbar.setTitle("Change city");
        setSupportActionBar(tToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etChangeLocation = findViewById(R.id.et_change_location);
    }

    @Override
    public void onClick(View view) {
        String city = etChangeLocation.getText().toString();
        if (!city.isEmpty()){
            Intent intent = getIntent();
            intent.putExtra(KEY, city);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}