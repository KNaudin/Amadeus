import argparse
import uuid
import hjson

from pytradfri.api.libcoap_api import APIFactory
from pytradfri.error import PytradfriError

if __name__ == '__main__':
	data = {}
	# recupere IP argparse
	parser = argparse.ArgumentParser()
	parser.add_argument('host', metavar='IP', type=str,
						help='IP Address of your Tradfri gateway')
	parser.add_argument('-K', dest='key',
						help='Security code found on your Tradfri gateway')
	args = parser.parse_args()
	identity = uuid.uuid4().hex
	api_factory = APIFactory(host=args.host, psk_id=identity)
	try:
		psk = api_factory.generate_psk(args.key)

		conf = {'identity': identity, 'key': psk}
		data[args.host] = conf
		with open('./gateway.conf', 'w') as f:
			hjson.dump(data, f)
		
	except:
		raise PytradfriError("Could not connect to the gateway. Please verify that you input the correct IP address and security key.")
