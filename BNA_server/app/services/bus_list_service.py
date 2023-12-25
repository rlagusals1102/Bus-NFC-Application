import httpx, json
import xml.etree.ElementTree as ET
from app.deps import BUS_API_URL


async def bus_list(route_id: str, stId: str):
    url = f"{BUS_API_URL}&busRouteId={route_id}"

    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    response_content = response.text

    try:
        root = ET.fromstring(response_content)
        stNm = None

        for itemList in root.findall('.//itemList'):
            if itemList.find('stId').text == stId:
                stNm = itemList.find('stNm').text

        return await route_information(stNm, stId)

    except ET.ParseError:
        return {"error": "Error parsing XML."}


async def route_information(name: str, stId: str):
    res = []

    try:
        file_path = "app/static/BusInfoData.json"
        with open(file_path, 'r', encoding='utf-8') as json_file:
            data = json.load(json_file)

            for item in data:
                if item['정류소명'] == name and item['NODE_ID'] == stId:
                    res.append(item['노선명'])

            return list(res)
    except FileNotFoundError:
        print({file_path})
        return None
