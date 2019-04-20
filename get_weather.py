import hjson
import requests
import os
import sys
from time import time

sys.path.append(os.path.realpath(__file__))

from _api import API_KEY

AMADEUS_PATH = os.path.realpath(os.path.dirname(__file__))
BUILD_ADDRESS = True
config = {}

# Checks for a config file containing the address of the user
if os.path.exists(os.path.join(AMADEUS_PATH, 'config.info')):
    BUILD_ADDRESS = False
    # The config file may be created but that doesn't mean that the address is set
    with open(os.path.join(AMADEUS_PATH, 'config.info'), 'r') as info_file:
        config = hjson.load(info_file)
    if not config['address']:
        BUILD_ADDRESS = True

# We supposedly need to find the user address (won't be required when everything is done by the phone later on)
if BUILD_ADDRESS:
    data = []
    while not data:
        print('Enter your address with no commas or abreviations :')
        address = input().replace(' ', '+')
        # We request the openstreetmap API with our address
        r = requests.get('https://nominatim.openstreetmap.org/search?q={}&format=json'.format(address))
        data = hjson.loads(r.text)
        # No address matches the one given so we try again until we provide something correct
        if not data:
            print('Invalid address. Try again.')
    # We create the config file with the address and its longitude and latitude
    with open('config.info', 'w') as info_file:
        print('Address confirmed. Configuring new address.')
        config['address'] = address
        config['lat'] = data[0]['lat']
        config['lon'] = data[0]['lon']
        info_file.write('{\n    address : "'+address+'",\n    lat : '+config['lat']+'\n    lon : '+config['lon']+'\n}')

# We request the openweathermap API with our API key to get the weather at the exact longitude and latitude
r = requests.get('http://api.openweathermap.org/data/2.5/weather?lat={}&lon={}&units=metric&appid={}'.format(config['lat'], config['lon'], API_KEY))
# If the request is correct, the HTTP status code will be 200
if r.status_code is 200:
    # We retrieve the data in the API's answer and create a weather.json file
    weather = hjson.loads(r.text)
    weather['timestamp'] = time()
    with open(os.path.join(AMADEUS_PATH, 'weather.json'), 'w') as f:
        hjson.dump(weather, f)
