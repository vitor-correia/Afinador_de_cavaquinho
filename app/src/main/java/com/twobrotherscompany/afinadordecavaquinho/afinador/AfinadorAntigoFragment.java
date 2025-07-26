package com.twobrotherscompany.afinadordecavaquinho.afinador;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.twobrotherscompany.afinadordecavaquinho.R;
import com.twobrotherscompany.afinadordecavaquinho.RegistroFirebase;

public class AfinadorAntigoFragment extends Fragment {

    private MediaPlayer mediaPlayersi;
    private MediaPlayer mediaPlayersol;
    private MediaPlayer mediaPlayerre;
    private MediaPlayer mediaPlayerrezinha;
    private Switch switchRepetição;

    private Button buttonCordaRezona, buttonCordaSol, buttonCordaSi, buttonCordaRezinha;

    private boolean isPro;

    private FrameLayout adContainerView;
    private AdView bannerView;

    public AfinadorAntigoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RegistroFirebase registroFirebase = RegistroFirebase.getInstance(getContext());
        registroFirebase.registrarVisualizacaoTela("Afinador", "AfinadorFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_afinador_antigo, container, false);

        adContainerView = view.findViewById(R.id.ad_view_container);
        setupBannerAd();

        mediaPlayerrezinha = MediaPlayer.create(getContext(), R.raw.cordaquatro);
        mediaPlayersi = MediaPlayer.create(getContext(), R.raw.cordatres);
        mediaPlayersol = MediaPlayer.create(getContext(), R.raw.cordadois);
        mediaPlayerre = MediaPlayer.create(getContext(), R.raw.cordaum);

        switchRepetição = view.findViewById(R.id.switchRepeticao);
        switchRepetição.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerre.stop();
                mediaPlayersol.stop();
                mediaPlayersi.stop();
                mediaPlayerrezinha.stop();
            }
        });

        buttonCordaRezona = view.findViewById(R.id.buttonCordaRezona);
        buttonCordaRezona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerre.stop();

                if (mediaPlayerre != null) {
                    mediaPlayerre = MediaPlayer.create(getContext(), R.raw.cordaum);
                    mediaPlayerre.start();

                    mediaPlayerrezinha.stop();
                    mediaPlayersi.stop();
                    mediaPlayersol.stop();

                } if (switchRepetição.isChecked()) {
                    mediaPlayerre.setLooping(true);
                }
            }
        });

        buttonCordaSol = view.findViewById(R.id.buttonCordaSol);
        buttonCordaSol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayersol.stop();

                if (mediaPlayersol != null) {
                    mediaPlayersol = MediaPlayer.create(getContext(), R.raw.cordadois);
                    mediaPlayersol.start();

                    mediaPlayerrezinha.stop();
                    mediaPlayersi.stop();
                    mediaPlayerre.stop();

                } if (switchRepetição.isChecked()) {
                    mediaPlayersol.setLooping(true);
                }
            }
        });

        buttonCordaSi = view.findViewById(R.id.buttonCordaSi);
        buttonCordaSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayersi.stop();

                if (mediaPlayersi != null) {
                    mediaPlayersi = MediaPlayer.create(getContext(), R.raw.cordatres);
                    mediaPlayersi.start();

                    mediaPlayerrezinha.stop();
                    mediaPlayersol.stop();
                    mediaPlayerre.stop();

                } if (switchRepetição.isChecked()) {
                    mediaPlayersi.setLooping(true);
                }
            }
        });

        buttonCordaRezinha = view.findViewById(R.id.buttonCordaRezinha);
        buttonCordaRezinha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerrezinha.stop();

                if (mediaPlayerrezinha != null) {
                    mediaPlayerrezinha = MediaPlayer.create(getContext(), R.raw.cordaquatro);
                    mediaPlayerrezinha.start();

                    mediaPlayersi.stop();
                    mediaPlayersol.stop();
                    mediaPlayerre.stop();

                } if (switchRepetição.isChecked()) {
                    mediaPlayerrezinha.setLooping(true);
                }
            }
        });

        return view;
    }

    private void setupBannerAd() {
        AdSize adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(getContext(), 320); //320 banner quadrado

        bannerView = new AdView(getContext());
        bannerView.setAdUnitId("xx");
        bannerView.setAdSize(adSize);

        adContainerView.removeAllViews();
        adContainerView.addView(bannerView);

        AdRequest adRequest = new AdRequest.Builder().build();
        bannerView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                RegistroFirebase registroFirebase = RegistroFirebase.getInstance(getContext());
                Bundle parametros = new Bundle();
                parametros.putString("Erro", adError.getMessage() + " " + adError.getCode());
                registroFirebase.registrarEvento("Erro_banner_Afinador_antigo", parametros);
                Log.e("BannerAd", "Erro ao carregar banner: " + adError.getMessage());
            }
        });
        bannerView.loadAd(adRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayersi.stop();
        mediaPlayersol.stop();
        mediaPlayerre.stop();
        mediaPlayerrezinha.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isPro = prefs.getBoolean("isPro", false); // false = valor padrão se não existir

        if (isPro) {

        } else {
            setupBannerAd();
        }
    }
}