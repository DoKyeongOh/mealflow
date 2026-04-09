import { createSlice } from '@reduxjs/toolkit'

export interface UiState {
  sidebarCollapsed: boolean
}

const initialState: UiState = {
  sidebarCollapsed: false,
}

export const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleSidebar(state) {
      state.sidebarCollapsed = !state.sidebarCollapsed
    },
  },
})

export const { toggleSidebar } = uiSlice.actions
