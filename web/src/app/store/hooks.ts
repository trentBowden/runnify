import { useDispatch, useSelector } from "react-redux";
import type { RootState, AppDispatch } from "./store";

/**
 * Custom hooks for the store.
 * These are used to get the dispatch and selector functions for the store.
 * The withTypes function is used to ensure that the dispatch and selector
 * functions are typed correctly.
 *
 * @see https://react.dev/learn/reusing-logic-with-custom-hooks
 */
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
export const useAppSelector = useSelector.withTypes<RootState>();
