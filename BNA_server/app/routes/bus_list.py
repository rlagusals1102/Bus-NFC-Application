from fastapi import APIRouter, Query, HTTPException
from app.services.bus_list_service import bus_list

router = APIRouter()


@router.get("/bus_list")
async def bus_list_routes(
        route_id: str = Query(..., description="Route ID as a string"),
        stId: str = Query(..., description="Station ID as a string")
):
    if not route_id or not stId:
        raise HTTPException(status_code=400, detail="Invalid input: route_id and stId must be provided.")
    else:
        result = await bus_list(route_id, stId)
        return result
