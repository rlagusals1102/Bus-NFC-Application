import httpx
import xml.etree.ElementTree as ET
from app.utilities.keys import BUS_API_URL
from app.utilities.deps import extract_bus_info

async def bus_finder(route_id: str, stId: str):
    url = f"{BUS_API_URL}&busRouteId={route_id}"
    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    response_content = response.text

    try:
        root = ET.fromstring(response_content)
        items = root.findall(".//itemList")

        for i, item in enumerate(items):
            if item.find('stId').text == stId:
                prev_info = await extract_bus_info(items[i - 1]) if i > 0 else None
                next_info = await extract_bus_info(items[i + 1]) if i < len(items) - 1 else None

                return {
                    "prev_info": prev_info,
                    "next_info": next_info
                }

        return {"error": "No matching data found."}

    except ET.ParseError:
        return {"error": "Error parsing XML."}

    except httpx.HTTPError as http_err:
        return {"error": f"HTTP error: {http_err}"}

    except Exception as e:
        return {"error": f"An unexpected error occurred: {str(e)}"}
