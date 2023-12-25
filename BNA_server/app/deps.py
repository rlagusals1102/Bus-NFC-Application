import os

SERVICE_KEY = os.environ.get("BUS_SERVICE_KEY")
BUS_API_URL = f"http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll?serviceKey={SERVICE_KEY}"

route_type_colors = {
    '1': '#00a0e9',
    '2': '#53b332',
    '3': '#0068b7',
    '4': '#53b332',
    '5': '#f2b70a',
    '6': '#e60012'
}
