from fastapi import APIRouter, Query, HTTPException
from app.services.bus_api_service import bus_finder

router = APIRouter()


@router.get("/bus_finder")
async def bus_finder_routes(
        route_id: str = Query(..., description="Route ID as a string"),
        stId: str = Query(..., description="Station ID as a string")
):
    if not route_id or not stId:
        raise HTTPException(status_code=400, detail="Invalid input: route_id and stId must be provided.")
    else:
        result = await bus_finder(route_id, stId)
        return result
