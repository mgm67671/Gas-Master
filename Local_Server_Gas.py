from flask import Flask, jsonify, request
import asyncio
from gasbuddy import GasBuddy

app = Flask(__name__)

async def get_gas_station_prices(station_id):
    gasbuddy = GasBuddy(station_id)
    prices = await gasbuddy.price_lookup()
    normalized_prices = {
        'regular_gas': prices.get('regular_gas'),
        'midgrade_gas': prices.get('midgrade_gas'),
        'premium_gas': prices.get('premium_gas'),
        'diesel': prices.get('diesel')
    }
    return normalized_prices

async def get_gas_prices(zip_code):
    gasbuddy = GasBuddy()
    results = await gasbuddy.location_search(zipcode=zip_code)
    if 'data' in results and results['data'] and 'locationBySearchTerm' in results['data']:
        stations = results['data']['locationBySearchTerm']['stations']['results']
        station_list = []
        for station in stations[:15]: # Update this number to change the number of stations retrieved
            prices = await get_gas_station_prices(station['id'])
            station_info = {
                'stationName': station.get('name', 'Unknown Station'),
                'stationId': station.get('id', 'Unknown ID'),
                'address': station.get('address', {'line1': 'Unknown Address'}),
                'prices': prices
            }
            station_list.append(station_info)
        return {'stations': station_list}
    return {'error': 'No stations found for the given ZIP code.'}

@app.route('/getGasPrices')
def get_gas_prices_route():
    zip_code = request.args.get('zipcode')
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    result = loop.run_until_complete(get_gas_prices(zip_code))
    print(result)
    return jsonify(result) # Returns a Json with the gas stations and information

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
