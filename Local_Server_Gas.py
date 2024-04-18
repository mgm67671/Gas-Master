from flask import Flask, jsonify, request
import asyncio
from gasbuddy import GasBuddy

app = Flask(__name__)

async def get_gas_station_prices(station_id):
    gasbuddy = GasBuddy(station_id)
    prices = await gasbuddy.price_lookup()
    return prices

async def get_gas_prices(zip_code):
    gasbuddy = GasBuddy()
    results = await gasbuddy.location_search(zipcode=zip_code)
    if 'data' in results and results['data'] and 'locationBySearchTerm' in results['data']:
        stations = results['data']['locationBySearchTerm']['stations']['results']
        if stations:
            station = stations[0]
            prices = await get_gas_station_prices(station['id'])
            return {'station': station, 'prices': prices}
    return {}

@app.route('/getGasPrices')
def get_gas_prices_route():
    zip_code = request.args.get('zipcode')
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    result = loop.run_until_complete(get_gas_prices(zip_code))
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)

