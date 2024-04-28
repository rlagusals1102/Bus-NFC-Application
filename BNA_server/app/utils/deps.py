"""
NOTE : 반복되는 코드 정의
"""
from fastapi import HTTPException

pattern = r"^\d{9}$"

bus_colors = {
    '1': '#00a0e9', # 공항 버스
    '2': '#53b332', # 지선 버스
    '3': '#0068b7', # 간선 버스
    '4': '#53b332', # 마을 버스
    '5': '#f2b70a', # 순환 버스
    '6': '#e60012'  # 광역 버스
}

async def extract_bus_info(item):
    return {
        'routeType': item.find('routeType').text,
        'routeColor': bus_colors.get(item.find('routeType').text, 'Unknown'),
        'rtNm': item.find('rtNm').text,
        'stId': item.find('stId').text,
        'stNm': item.find('stNm').text,
        'arrmsg1': item.find('arrmsg1').text,
        'arrmsg2': item.find('arrmsg2').text
    }

async def handle_exceptions(e: Exception) -> HTTPException:
    error = str(e)
    if "not found" in error:
        raise HTTPException(status_code=404, detail=error)

    elif "validation error" in error:
        raise HTTPException(status_code=422, detail=error)
    
    else:
        raise HTTPException(status_code=500, detail=error)