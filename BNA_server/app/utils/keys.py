import os
SERVICE_KEY = os.getenv("BUS_SERVICE_KEY")
BUS_API_URL = f"http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll?serviceKey={SERVICE_KEY}"
BUS_INFO_JSON = r"BNA_server\app\services\BusInfoData.json"
