import hjson
import requests
import os

from time import time
from _api import API_KEY

BUILD_ADDRESS = True
config = {}

if os.path.exists('config.info'):
    BUILD_ADDRESS = False
    with open('config.info', 'r') as info_file:
        config = hjson.load(info_file)
    if not config['address']:
        BUILD_ADDRESS = True

if BUILD_ADDRESS:
    data = []
    while not data:
        print('Enter your address with no commas or abreviations :')
        address = input().replace(' ', '+')
        r = requests.get('https://nominatim.openstreetmap.org/search?q={}&format=json'.format(address))
        data = hjson.loads(r.text)
        if not data:
            print('Invalid address. Try again.')
    with open('config.info', 'w') as info_file:
        print('Address confirmed. Configuring new address.')
        config['address'] = address
        config['lat'] = data[0]['lat']
        config['lon'] = data[0]['lon']
        info_file.write('{\n    address : "'+address+'",\n    lat : '+config['lat']+'\n    lon : '+config['lon']+'\n}')

r = requests.get('http://api.openweathermap.org/data/2.5/weather?lat={}&lon={}&units=metric&appid={}'.format(config['lat'], config['lon'], API_KEY))
if r.status_code is 200:
    weather = hjson.loads(r.text)
    weather['timestamp'] = time()
    with open('weather.json', 'w') as f:
        hjson.dump(weather, f)
