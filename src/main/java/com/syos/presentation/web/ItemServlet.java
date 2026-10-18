package com.syos.presentation.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/items/*")
public class ItemServlet extends HttpServlet {

    private ItemRepository itemRepo;
    private Gson gson;

    @Override
    public void init() {
        itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/items — return all items
                List<Item> items = itemRepo.findAll();
                resp.getWriter().write(gson.toJson(items));

            } else {
                // GET /api/items/{code} — return single item
                String code = pathInfo.substring(1);
                var item = itemRepo.findByCode(code);

                if (item.isPresent()) {
                    resp.getWriter().write(gson.toJson(item.get()));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Item not found: " + code + "\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}