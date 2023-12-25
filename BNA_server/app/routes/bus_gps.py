from fastapi import APIRouter, Query, HTTPException
from app.services.json_service import get_bus_gps

router = APIRouter()


@router.get("/bus_gps")
async def bus_gps_route(
        route_id: str = Query(..., description="Route ID as a string"),
        stId: str = Query(..., description="Station ID as a string")
):
    if not route_id or not stId:
        raise HTTPException(status_code=400, detail="Invalid input: route_id and stId must be provided.")
    else:
        result = await get_bus_gps(route_id, stId)
        return result
