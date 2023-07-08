package com.example.configuration;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
public class TalkResource {

private static final String VOICENAME_kevin = "kevin16";
private final String text; // string to speech

public TalkResource(String text) {
    this.text = text;
}

public void speak() {
    System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
    Voice voice;
    VoiceManager voiceManager = VoiceManager.getInstance();
    voice = voiceManager.getVoice(VOICENAME_kevin);
    voice.allocate();

    String newText = "example";
    voice.speak(newText);
    }
}