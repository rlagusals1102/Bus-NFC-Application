from flask import Flask, request, Response, jsonify
import json
import requests
import xml.etree.ElementTree as ET

app = Flask(__name__)

#(api가 없어 구현)
def extract_routes_for_station(data, station_name):
    with open(data, 'r', encoding='utf-8') as json_file:
        bus_data = json.load(json_file)

    routes = {route['노선명'] for route in bus_data if route['정류소명'] == station_name}

    return list(routes)

# 해당 정류장의 버스들 종류 가져오는 함수
@app.route('/get_routes', methods=['GET'])
def get_routes():
    data_file = 'output.json'
    station_name_to_search = request.args.get('station_name')

    if station_name_to_search is None:
        return jsonify({'error': 'Please provide a valid station_name parameter.'}), 400

    result_routes = extract_routes_for_station(data_file, station_name_to_search)

    response_data = {'result_routes': result_routes}
    response_json = json.dumps(response_data, ensure_ascii=False)

    return Response(response_json, content_type='application/json; charset=utf-8')

# mod : 버스 노선 값
# targetArsId : 정류장 고유 값
@app.route('/get_mod', methods=['GET'])
def getBusInfo():
    mod = request.args.get('mod')
    targetArsId = request.args.get('arsId')

    url = 'http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll'
    params = {
        'serviceKey': 'WVI7ZtvAVX6hik3qi1Y37dBT8JHku9C+WhfM2MKgmcnMqJvckqqUdOpGAf9EpWdzA5gsaTyth86/Jnvo10Xxwg==',
        'busRouteId': mod
    }

    response = requests.get(url, params=params)
    root = ET.fromstring(response.text)[2]

    for i in range(0, len(root)):
        arsId = root[i][2].text

        if (arsId == targetArsId):
            idx1 = max(0, i - 1);
            idx2 = min(len(root) - 1, i + 1)
            return ",".join([
                root[idx1].findall("arsId")[0].text,
                root[idx1].findall("stNm")[0].text,
                root[idx2].findall("arsId")[0].text,
                root[idx2].findall("stNm")[0].text
            ])
    return "{}"

# 해당 현 버스 위치를 기준으로 전 버스 정류장과 다음 정류장 도착 알림
@app.route('/get_ars_id_info', methods=['GET'])
def getArsIdInfo():
    mod = request.args.get('mod')
    targetArsId = request.args.get('arsId')

    url = 'http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll'
    params = {
        'serviceKey': 'WVI7ZtvAVX6hik3qi1Y37dBT8JHku9C+WhfM2MKgmcnMqJvckqqUdOpGAf9EpWdzA5gsaTyth86/Jnvo10Xxwg==',
        'busRouteId': mod
    }

    response = requests.get(url, params=params)
    root = ET.fromstring(response.text)[2]

    for i in range(0, len(root)):
        arsId = root[i][2].text

        if (arsId == targetArsId):
            return ",".join([
                root[i].findall("rtNm")[0].text,
                root[i].findall("arrmsg1")[0].text,
                root[i].findall("arrmsg2")[0].text,
                root[i].findall("brdrde_Num1")[0].text,
                root[i].findall("brdrde_Num2")[0].text
            ])
    return "{}"

if __name__ == '__main__':
    host = '0.0.0.0'
    port = 5000
    app.run(debug=True, host=host, port=port)