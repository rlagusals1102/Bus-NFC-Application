from fastapi import APIRouter, Query, HTTPException
from app.services.bus_api_service import get_bus_info

router = APIRouter()


@router.get("/bus_info")
async def get_bus_info_route(
        route_id: str = Query(..., description="Route ID as a string"),
        stId: str = Query(..., description="Station ID as a string")
):
    try:
        bus_info = await get_bus_info(route_id, stId)
        return bus_info
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
