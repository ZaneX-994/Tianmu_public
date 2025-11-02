// const danmuWSBaseUrl = "ws://localhost:9101/ws/bulletScreen/"
const danmuWSBaseUrl = "ws://127.0.0.1:9101/ws/bulletScreen/"

export function getDanmuWSUrl(videoId) {
    return `${danmuWSBaseUrl}${videoId}`
}

export function getSearchHistoryKey() {
    return "TIANMU_searchHistories"
}

export const TIANMU_USER = "TIANMU_USER"
export const TIANMU_LOGIN = "TIANMU_LOGIN"