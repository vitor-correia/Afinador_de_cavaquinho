package com.twobrotherscompany.afinadordecavaquinho.afinador;

import static android.view.View.GONE;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.twobrotherscompany.afinadordecavaquinho.R;
import com.twobrotherscompany.afinadordecavaquinho.RegistroFirebase;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class AfinadorFragment extends Fragment {
    private TextView tvNote, tvFrequency, tvTuningStatus;
    private volatile boolean isRecording = false;
    private AudioRecord audioRecord;
    private Thread recordingThread;

    // Configurações de áudio
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = 4096; // Potência de 2 (2^12) para a FFT

    private boolean isPro;

    private final double[] TARGET_FREQUENCIES = {293.66, 392.00, 493.88, 587.33};
    private final String[] TARGET_NOTES = {"D", "G", "B", "D"};
    private final double TOLERANCE = 5.0; // Margem de erro em Hz

    private ImageView imageLuz1, imageLuz2, imageLuz3, imageLuz4;

    private AdView adView;
    private FrameLayout adContainerView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (canAccessMicrophone()) {
                        startRecordingProcess();
                    } else {
                        Toast.makeText(getContext(), "Não foi possível acessar o microfone",
                                Toast.LENGTH_LONG).show();
                        resetUI();
                    }
                } else {
                    Toast.makeText(getContext(), "Permissão de áudio negada. O afinador não funcionará.",
                            Toast.LENGTH_LONG).show();
                    resetUI();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_afinador, container, false);

        adContainerView = view.findViewById(R.id.ad_view_container);

        RegistroFirebase registroFirebase = RegistroFirebase.getInstance(getContext());
        registroFirebase.registrarVisualizacaoTela("Afinador-Fragment", "AfinadorFragment");

        Button buttonAfinadorAntigo = view.findViewById(R.id.buttonAfinadorAntigo);
        buttonAfinadorAntigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment newUsr = new AfinadorAntigoFragment();
                transaction.addToBackStack(null);
                transaction.replace(R.id.frameConteudo, newUsr);
                transaction.commit();
            }
        });

        tvNote = view.findViewById(R.id.tvNote);
        tvFrequency = view.findViewById(R.id.tvFrequency);
        tvFrequency.setVisibility(GONE);
        tvTuningStatus = view.findViewById(R.id.tvTuningStatus);

        toggleRecording();

        imageLuz1 = view.findViewById(R.id.imageLuz_1);
        imageLuz2 = view.findViewById(R.id.imageLuz_2);
        imageLuz3 = view.findViewById(R.id.imageLuz_3);
        imageLuz4 = view.findViewById(R.id.imageLuz_4);

        resetLights();

        return view;
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
            resetUI();
            isRecording = false;
        } else {
            checkAndRequestAudioPermission();
        }
    }

    private void resetUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvNote.setText("");
                tvFrequency.setText("Frequência: 0 Hz");
                tvTuningStatus.setText("");
            });
        }
    }

    private boolean canAccessMicrophone() {
        try {
            AudioRecord recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE);

            boolean canAccess = recorder.getState() == AudioRecord.STATE_INITIALIZED;
            recorder.release();
            return canAccess;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkAndRequestAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (canAccessMicrophone()) {
                startRecordingProcess();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecordingProcess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
            });
        }

        if (startRecording()) {
            isRecording = true;
        } else {
            resetUI();
        }
    }

    private boolean startRecording() {
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    CHANNEL_CONFIG, AUDIO_FORMAT);

            if (BUFFER_SIZE < minBufferSize) {
                showToast("Buffer size muito pequeno para esta configuração");
                return false;
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                audioRecord = new AudioRecord.Builder()
                        .setAudioSource(MediaRecorder.AudioSource.MIC)
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AUDIO_FORMAT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(CHANNEL_CONFIG)
                                .build())
                        .setBufferSizeInBytes(BUFFER_SIZE)
                        .build();
            } else {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        BUFFER_SIZE);
            }

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new Exception("Falha ao inicializar AudioRecord");
            }

            audioRecord.startRecording();

            recordingThread = new Thread(() -> {
                short[] audioBuffer = new short[BUFFER_SIZE];
                while (isRecording && !Thread.currentThread().isInterrupted()) {
                    int samplesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
                    if (samplesRead > 0) {
                        double pitch = calculatePitch(audioBuffer);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> processPitch(pitch));
                        }
                    }
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erro ao iniciar gravação: " + e.getMessage());
            stopRecording();
            return false;
        }
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
        }
    }

    private void stopRecording() {
        isRecording = false;

        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                audioRecord = null;
            }
        }

        if (recordingThread != null) {
            try {
                recordingThread.interrupt();
                recordingThread.join(100);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                recordingThread = null;
            }
        }
    }

    private double calculatePitch(short[] audioData) {
        try {
            int fftSize = 1;
            while (fftSize < audioData.length) {
                fftSize <<= 1;
            }

            fftSize = Math.min(fftSize, 8192);

            double[] doubleData = new double[fftSize];

            for (int i = 0; i < Math.min(audioData.length, fftSize); i++) {
                doubleData[i] = audioData[i] / 32768.0 *
                        (0.5 * (1 - Math.cos(2 * Math.PI * i / (fftSize - 1))));
            }

            for (int i = audioData.length; i < fftSize; i++) {
                doubleData[i] = 0;
            }

            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] complexResult = fft.transform(doubleData, TransformType.FORWARD);

            double maxMagnitude = -1;
            int peakIndex = 0;
            int minFreqIndex = (int)(50 * fftSize / (double)SAMPLE_RATE);
            int maxFreqIndex = (int)(1000 * fftSize / (double)SAMPLE_RATE);

            for (int i = minFreqIndex; i < Math.min(complexResult.length / 2, maxFreqIndex); i++) {
                double magnitude = complexResult[i].abs();
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                    peakIndex = i;
                }
            }

            return (double) peakIndex * SAMPLE_RATE / fftSize;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void processPitch(double pitchInHz) {
        if (pitchInHz <= 50 || pitchInHz > 1000) {
            tvNote.setText("");
            tvFrequency.setText("Frequência: 0 Hz");
            tvTuningStatus.setText("Nenhum som detectado");
            resetLights();
            return;
        }

        tvFrequency.setText(String.format("Frequência: %.1f Hz", pitchInHz));

        int closestNoteIndex = 0;
        double minDifference = Double.MAX_VALUE;
        for (int i = 0; i < TARGET_FREQUENCIES.length; i++) {
            double diff = Math.abs(TARGET_FREQUENCIES[i] - pitchInHz);
            if (diff < minDifference) {
                minDifference = diff;
                closestNoteIndex = i;
            }
        }

        String closestNote = TARGET_NOTES[closestNoteIndex];
        double targetFreq = TARGET_FREQUENCIES[closestNoteIndex];
        double difference = pitchInHz - targetFreq;

        tvNote.setText("" + closestNote);

        updateLights(closestNoteIndex, difference);

        if (Math.abs(difference) <= TOLERANCE) {
            tvTuningStatus.setText("AFINADO! ✅");
        } else if (difference > 0) {
            //tvTuningStatus.setText(String.format("Status: Alto (afrouxe %.1f Hz) ⬇️", difference));
            tvTuningStatus.setText(String.format("Afrouxe a corda ⬇️", difference));
        } else {
            tvTuningStatus.setText(String.format("Aperte a corda ⬆️", Math.abs(difference)));
        }

        if (pitchInHz <= 140){
            tvTuningStatus.setText(String.format("Detectando corda..."));
            tvNote.setText("");
            resetLights();
        }
    }

    private void resetLights() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                imageLuz1.setImageResource(R.drawable.luz_apagada);
                imageLuz2.setImageResource(R.drawable.luz_apagada);
                imageLuz3.setImageResource(R.drawable.luz_apagada);
                imageLuz4.setImageResource(R.drawable.luz_apagada);
            });
        }
    }

    private void updateLights(int noteIndex, double difference) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Primeiro, resetar todas as luzes
                resetLights();

                // Se estiver dentro da tolerância, acender a luz correspondente
                if (Math.abs(difference) <= TOLERANCE) {
                    switch (noteIndex) {
                        case 0: // 293.66 Hz - Luz 1
                            imageLuz1.setImageResource(R.drawable.luz_acesa);
                            break;
                        case 1: // 392.00 Hz - Luz 2
                            imageLuz2.setImageResource(R.drawable.luz_acesa);
                            break;
                        case 2: // 493.88 Hz - Luz 3
                            imageLuz3.setImageResource(R.drawable.luz_acesa);
                            break;
                        case 3: // 587.33 Hz - Luz 4
                            imageLuz4.setImageResource(R.drawable.luz_acesa);
                            break;
                    }
                }
            });
        }
    }

    public void bannerAd(){
        adView = new AdView(requireContext());
        adView.setAdUnitId("xx");
        adContainerView.addView(adView);
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                RegistroFirebase registroFirebase = RegistroFirebase.getInstance(getContext());
                Bundle parametros = new Bundle();
                parametros.putString("Erro", adError.getMessage() + " " + adError.getCode());
                registroFirebase.registrarEvento("Erro_banner_Afinador_novo", parametros);
                Log.e("BannerAd", "Erro ao carregar banner: " + adError.getMessage());
            }
        });
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isPro = prefs.getBoolean("isPro", false); // false = valor padrão se não existir

        if (isPro) {

        } else {
            bannerAd();
        }
    }
}