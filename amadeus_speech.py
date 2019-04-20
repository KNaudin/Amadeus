import speech_recognition as sr
import pyglet
import sys
import os

sys.path.append(os.path.realpath(__file__))
AMADEUS_PATH = os.path.realpath(os.path.dirname(__file__))
GIVEN_NAME = 'Amadeus'

# This loads the audio of Amadeus' possible answers
amadeus_answers = {
    'yes' : pyglet.media.load(os.path.join(AMADEUS_PATH, 'raw/yes.ogg'), streaming=False),
    'sorry' : pyglet.media.load(os.path.join(AMADEUS_PATH, 'raw/sorry.ogg'), streaming=False),
    'hello' : pyglet.media.load(os.path.join(AMADEUS_PATH, 'raw/hello.ogg'), streaming=False)
}

# Setting up the recognizer and the default computer's microphone to work with PyAudio
recognizer = sr.Recognizer()
microphone = sr.Microphone()

# Transcription contains the text that the recognizer will retrieve
transcription = []
stop_recording = False
with microphone as source:
    # Noise reduction
    recognizer.adjust_for_ambient_noise(source)
    # Amadeus says 'Hello!' at the startup
    amadeus_answers['hello'].play()
    while not stop_recording:
        listening_to_command = False
        print('Dites quelque chose.')
        # Listen to any audio from the microphone, resets every 5 seconds to not be stuck in a noise loop
        try:
            audio = recognizer.listen(source, timeout=5)
        except:
            continue

        try:
            # If the Google API recognize the word 'Amadeus' in what we just says, then we're ready to hear a command
            transcription = recognizer.recognize_google(audio, language='fr-FR')
            if GIVEN_NAME in transcription:
                listening_to_command = True
                print('Amadeus is listening.')
                # Amadeus says 'はい' / 'hai' / 'yes' so we know it's listening
                amadeus_answers['yes'].play()
                audio = recognizer.listen(source, timeout=10)
                command = recognizer.recognize_google(audio, language='fr-FR')
                print('Amadeus heard: {}'.format(command))
        # If the API can't process because of a connection error
        except sr.RequestError:
            print('API unavailable')
        # If the API can't process because it can't understand the audio
        except sr.UnknownValueError:
            print('Unable to recognize speech')
            # Amadeus says 'ごめん' / 'gomen' / 'sorry' so we know it didn't get what we're trying to say
            if listening_to_command:
                amadeus_answers['sorry'].play()
        # If we say 'stop', then the recording stop. This is used for the debug and won't be in the release version
        if 'stop' in transcription:
            stop_recording = True

        transcription = []

print('Arrêt de l\'enregistrement')
