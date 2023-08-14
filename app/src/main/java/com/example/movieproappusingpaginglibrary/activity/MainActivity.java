package com.example.movieproappusingpaginglibrary.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.movieproappusingpaginglibrary.R;
import com.example.movieproappusingpaginglibrary.adapters.MoviesAdapter;
import com.example.movieproappusingpaginglibrary.adapters.MoviesLoadStateAdapter;
import com.example.movieproappusingpaginglibrary.databinding.ActivityMainBinding;
import com.example.movieproappusingpaginglibrary.util.GridSpace;
import com.example.movieproappusingpaginglibrary.util.MovieComparator;
import com.example.movieproappusingpaginglibrary.util.Utils;
import com.example.movieproappusingpaginglibrary.viewmodel.MovieViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    MovieViewModel mainActivityViewModel;
    MoviesAdapter moviesAdapter;

    @Inject
    RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(Utils.API_KEY == null || Utils.API_KEY.isEmpty()){
            Toast.makeText(this, "Error in API Key", Toast.LENGTH_SHORT).show();
        }

        moviesAdapter = new MoviesAdapter(new MovieComparator(), requestManager);

        mainActivityViewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        initRecyclerViewAndAdapter();

        // subscribe to paging data
        mainActivityViewModel.moviePagingDataFlowable.subscribe(moviePagingData ->
        {
            moviesAdapter.submitData(getLifecycle(), moviePagingData);
        } );
    }

    private void initRecyclerViewAndAdapter() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this , 2);

        binding.recyclerViewMovies.setLayoutManager(gridLayoutManager);

        binding.recyclerViewMovies.addItemDecoration(new GridSpace(2 , 20 , true));

        binding.recyclerViewMovies.setAdapter(
                moviesAdapter.withLoadStateFooter(
                        new MoviesLoadStateAdapter( view -> {
                            moviesAdapter.retry();
                        })
                )
        );

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return moviesAdapter.getItemViewType(position) == MoviesAdapter.LOADING_ITEM ? 1:2 ;
            }
        });
    }
}