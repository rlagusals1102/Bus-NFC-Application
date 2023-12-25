from fastapi import HTTPException

def handle_exceptions(e: Exception) -> HTTPException:
    error_detail = str(e)
    if "not found" in error_detail:
        return HTTPException(status_code=404, detail=error_detail)
    elif "validation error" in error_detail:
        return HTTPException(status_code=422, detail=error_detail)
    else:
        return HTTPException(status_code=500, detail=error_detail)
